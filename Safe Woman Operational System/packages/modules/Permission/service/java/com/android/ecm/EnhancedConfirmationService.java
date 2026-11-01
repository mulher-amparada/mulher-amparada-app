/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ecm;

import static android.app.ecm.EnhancedConfirmationManager.REASON_PACKAGE_RESTRICTED;
import static android.app.ecm.EnhancedConfirmationManager.REASON_PHONE_STATE;

import static com.android.permission.PermissionStatsLog.CALL_WITH_ECM_INTERACTION_REPORTED;
import static com.android.permissioncontroller.PermissionControllerStatsLog.ECM_RESTRICTION_QUERY_IN_CALL_REPORTED;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.FlaggedApi;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.annotation.UserIdInt;
import android.annotation.WorkerThread;
import android.app.AppOpsManager;
import android.app.ecm.EnhancedConfirmationManager;
import android.app.ecm.IEnhancedConfirmationManager;
import android.app.role.RoleManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.SignedPackage;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemConfigManager;
import android.os.UserHandle;
import android.permission.flags.Flags;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.PhoneLookup;
import android.telecom.Call;
import android.telecom.PhoneAccount;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import com.android.permission.util.UserUtils;
import com.android.permissioncontroller.PermissionControllerStatsLog;
import com.android.server.LocalManagerRegistry;
import com.android.server.SystemService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Service for ECM (Enhanced Confirmation Mode).
 *
 * @see EnhancedConfirmationManager
 *
 * @hide
 */
@Keep
@FlaggedApi(Flags.FLAG_ENHANCED_CONFIRMATION_MODE_APIS_ENABLED)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@SuppressLint("MissingPermission")
public class EnhancedConfirmationService extends SystemService {
    private static final String LOG_TAG = EnhancedConfirmationService.class.getSimpleName();

    /** A map of ECM states to their corresponding app op states */
    @Retention(java.lang.annotation.RetentionPolicy.SOURCE)
    @IntDef(prefix = {"ECM_STATE_"}, value = {EcmState.ECM_STATE_NOT_GUARDED,
            EcmState.ECM_STATE_GUARDED, EcmState.ECM_STATE_GUARDED_AND_ACKNOWLEDGED,
            EcmState.ECM_STATE_IMPLICIT})
    private @interface EcmState {
        int ECM_STATE_NOT_GUARDED = AppOpsManager.MODE_ALLOWED;
        int ECM_STATE_GUARDED = AppOpsManager.MODE_ERRORED;
        int ECM_STATE_GUARDED_AND_ACKNOWLEDGED = AppOpsManager.MODE_IGNORED;
        int ECM_STATE_IMPLICIT = AppOpsManager.MODE_DEFAULT;
    }

    private static final String EXEMPT_ALL_SETTINGS = "*";

    @VisibleForTesting
    final ArraySet<String> mPerPackageProtectedSettings = new ArraySet<>();

    // Settings restricted when an untrusted call is ongoing. These must also be added to
    // PROTECTED_SETTINGS
    @VisibleForTesting
    final ArraySet<String> mUntrustedCallRestrictedSettings = new ArraySet<>();

    private Map<String, List<byte[]>> mTrustedPackageCertDigests;
    private Map<String, List<byte[]>> mTrustedInstallerCertDigests;
    private static final int CALL_TYPE_UNTRUSTED = 0;
    private static final int CALL_TYPE_TRUSTED = 1;
    private static final int CALL_TYPE_EMERGENCY = 1 << 1;
    @IntDef(flag = true, value = {
            CALL_TYPE_UNTRUSTED,
            CALL_TYPE_TRUSTED,
            CALL_TYPE_EMERGENCY
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface CallType {}

    public EnhancedConfirmationService(@NonNull Context context) {
        super(context);
        LocalManagerRegistry.addManager(EnhancedConfirmationManagerLocal.class,
                new EnhancedConfirmationManagerLocalImpl(this));
    }

    private PackageManager mPackageManager;

    // A helper which tracks the calls received by the system, and information about them.
    private CallTracker mCallTracker;

    @GuardedBy("mUserAccessibilityManagers")
    private final Map<Integer, AccessibilityManager> mUserAccessibilityManagers =
            new ArrayMap<>();

    @Override
    public void onStart() {
        Context context = getContext();
        SystemConfigManager systemConfigManager = context.getSystemService(
                SystemConfigManager.class);
        mTrustedPackageCertDigests = toTrustedPackageMap(
                systemConfigManager.getEnhancedConfirmationTrustedPackages());
        mTrustedInstallerCertDigests = toTrustedPackageMap(
                systemConfigManager.getEnhancedConfirmationTrustedInstallers());
        initSettings(context);

        publishBinderService(Context.ECM_ENHANCED_CONFIRMATION_SERVICE, new Stub());

        if (Flags.unknownCallPackageInstallBlockingEnabled()) {
            mCallTracker = new CallTracker(getContext());
        }
        mPackageManager = getContext().getPackageManager();
    }

    private Map<String, List<byte[]>> toTrustedPackageMap(Set<SignedPackage> signedPackages) {
        ArrayMap<String, List<byte[]>> trustedPackageMap = new ArrayMap<>();
        for (SignedPackage signedPackage : signedPackages) {
            if (Flags.appFunctionAccessApiEnabled() && !signedPackage.hasCertificateDigest()) {
                // certs are mandatory for ECM
                continue;
            }
            ArrayList<byte[]> certDigests = (ArrayList<byte[]>) trustedPackageMap.computeIfAbsent(
                    signedPackage.getPackageName(), packageName -> new ArrayList<>(1));
            certDigests.add(signedPackage.getCertificateDigest());
        }
        return trustedPackageMap;
    }

    private static final String EXEMPT_SETTINGS_RESOURCE_NAME =
            "config_enhancedConfirmationModeExemptSettings";

    @VisibleForTesting
    void initSettings(@NonNull Context context) {
        // Runtime permissions
        mPerPackageProtectedSettings.add(Manifest.permission.SEND_SMS);
        mPerPackageProtectedSettings.add(Manifest.permission.RECEIVE_SMS);
        mPerPackageProtectedSettings.add(Manifest.permission.READ_SMS);
        mPerPackageProtectedSettings.add(Manifest.permission.RECEIVE_MMS);
        mPerPackageProtectedSettings.add(Manifest.permission.RECEIVE_WAP_PUSH);
        mPerPackageProtectedSettings.add(Manifest.permission.READ_CELL_BROADCASTS);
        mPerPackageProtectedSettings.add(Manifest.permission_group.SMS);

        mPerPackageProtectedSettings.add(Manifest.permission.BIND_DEVICE_ADMIN);
        // App ops
        mPerPackageProtectedSettings.add(AppOpsManager.OPSTR_BIND_ACCESSIBILITY_SERVICE);
        mPerPackageProtectedSettings.add(AppOpsManager.OPSTR_ACCESS_NOTIFICATIONS);
        mPerPackageProtectedSettings.add(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW);
        mPerPackageProtectedSettings.add(AppOpsManager.OPSTR_GET_USAGE_STATS);
        mPerPackageProtectedSettings.add(AppOpsManager.OPSTR_LOADER_USAGE_STATS);
        // Default application roles.
        mPerPackageProtectedSettings.add(RoleManager.ROLE_DIALER);
        mPerPackageProtectedSettings.add(RoleManager.ROLE_SMS);

        if (Flags.unknownCallPackageInstallBlockingEnabled()) {
            // Requesting package installs, limited during phone calls
            mUntrustedCallRestrictedSettings.add(
                    AppOpsManager.OPSTR_REQUEST_INSTALL_PACKAGES);
            mUntrustedCallRestrictedSettings.add(
                    AppOpsManager.OPSTR_BIND_ACCESSIBILITY_SERVICE);
        }
        loadPackageExemptSettings(context);
    }


    @VisibleForTesting
    void loadPackageExemptSettings(@NonNull Context context) {
        int resourceId = context.getResources().getIdentifier(EXEMPT_SETTINGS_RESOURCE_NAME,
                "array", "android");
        if (resourceId == 0) {
            return;
        }
        try {
            List<String> exemptSettings =
                    Arrays.asList(context.getResources().getStringArray(resourceId));
            for (String exemptSetting: exemptSettings) {
                if (EXEMPT_ALL_SETTINGS.equals(exemptSetting)) {
                    mPerPackageProtectedSettings.clear();
                    return;
                }
                mPerPackageProtectedSettings.remove(exemptSetting);
            }
        } catch (Resources.NotFoundException e) {
            Log.e(LOG_TAG, "Cannot get resource: " + EXEMPT_SETTINGS_RESOURCE_NAME, e);
        }
    }

    void addOngoingCall(Call call) {
        assertNotMainThread();
        if (mCallTracker != null) {
            mCallTracker.addCall(call);
        }
    }

    @WorkerThread
    void removeOngoingCall(String callId) {
        assertNotMainThread();
        if (mCallTracker != null) {
            mCallTracker.endCall(callId);
        }
    }

    @WorkerThread
    void clearOngoingCalls() {
        assertNotMainThread();

        if (mCallTracker != null) {
            mCallTracker.endAllCalls();
        }
    }

    static int getPackageUid(PackageManager pm, String packageName,
            int userId) throws NameNotFoundException {
        return pm.getPackageUidAsUser(packageName, PackageManager.PackageInfoFlags.of(0), userId);
    }

    private void assertNotMainThread() throws IllegalStateException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Ecm WorkerThread method called on main thread");
        }
    }

    private class Stub extends IEnhancedConfirmationManager.Stub {

        private final @NonNull Context mContext;
        private final String mAttributionTag;
        private final AppOpsManager mAppOpsManager;

        Stub() {
            Context context = getContext();
            mContext = context;
            mAttributionTag = context.getAttributionTag();
            mAppOpsManager = context.getSystemService(AppOpsManager.class);
        }

        public boolean isRestricted(@NonNull String packageName, @NonNull String settingIdentifier,
                @UserIdInt int userId) {
            return getRestrictionReason(packageName, settingIdentifier, userId) != null;
        }

        public String getRestrictionReason(@NonNull String packageName,
                @NonNull String settingIdentifier,
                @UserIdInt int userId) {
            enforcePermissions("isRestricted", userId);
            if (!UserUtils.isUserExistent(userId, getContext())) {
                Log.e(LOG_TAG, "user " + userId + " does not exist");
                return null;
            }

            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");
            Preconditions.checkStringNotEmpty(settingIdentifier,
                    "settingIdentifier cannot be null or empty");

            try {
                if (!isSettingEcmProtected(settingIdentifier)) {
                    return null;
                }
                if (isSettingEcmGuardedForPackage(settingIdentifier, packageName, userId)) {
                    return REASON_PACKAGE_RESTRICTED;
                }
                String globalProtectionReason =
                        getGlobalProtectionReason(settingIdentifier, packageName, userId);
                if (globalProtectionReason != null) {
                    return globalProtectionReason;
                }
                return null;
            } catch (NameNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public void clearRestriction(@NonNull String packageName, @UserIdInt int userId) {
            enforcePermissions("clearRestriction", userId);
            if (!UserUtils.isUserExistent(userId, getContext())) {
                return;
            }

            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");

            try {
                int state = getAppEcmState(packageName, userId);
                boolean isAllowed = state == EcmState.ECM_STATE_GUARDED_AND_ACKNOWLEDGED;
                if (!isAllowed) {
                    throw new IllegalStateException("Clear restriction attempted but not allowed");
                }
                setAppEcmState(packageName, EcmState.ECM_STATE_NOT_GUARDED, userId);
                EnhancedConfirmationStatsLogUtils.logRestrictionCleared(
                        getPackageUid(mPackageManager, packageName, userId));
            } catch (NameNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public boolean isClearRestrictionAllowed(@NonNull String packageName,
                @UserIdInt int userId) {
            enforcePermissions("isClearRestrictionAllowed", userId);
            if (!UserUtils.isUserExistent(userId, getContext())) {
                return false;
            }

            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");

            try {
                int state = getAppEcmState(packageName, userId);
                return state == EcmState.ECM_STATE_GUARDED_AND_ACKNOWLEDGED;
            } catch (NameNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public void setClearRestrictionAllowed(@NonNull String packageName, @UserIdInt int userId) {
            enforcePermissions("setClearRestrictionAllowed", userId);
            if (!UserUtils.isUserExistent(userId, getContext())) {
                return;
            }

            Preconditions.checkStringNotEmpty(packageName, "packageName cannot be null or empty");

            try {
                if (isPackageEcmGuarded(packageName, userId)) {
                    setAppEcmState(packageName, EcmState.ECM_STATE_GUARDED_AND_ACKNOWLEDGED,
                            userId);
                }
            } catch (NameNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private void enforcePermissions(@NonNull String methodName, @UserIdInt int userId) {
            UserUtils.enforceCrossUserPermission(userId, /* allowAll= */ false,
                    /* enforceForProfileGroup= */ false, methodName, mContext);
            mContext.enforceCallingOrSelfPermission(
                    android.Manifest.permission.MANAGE_ENHANCED_CONFIRMATION_STATES, methodName);
        }

        private boolean isPackageEcmGuarded(@NonNull String packageName, @UserIdInt int userId)
                throws NameNotFoundException {
            ApplicationInfo applicationInfo = getApplicationInfoAsUser(packageName, userId);
            // Always trust allow-listed and pre-installed packages
            if (isAllowlistedPackage(packageName) || isAllowlistedInstaller(packageName)
                    || isPackagePreinstalled(applicationInfo)) {
                return false;
            }

            // If the package already has an explicitly-set state, use that
            @EcmState int ecmState = getAppEcmState(packageName, userId);
            if (ecmState == EcmState.ECM_STATE_GUARDED
                    || ecmState == EcmState.ECM_STATE_GUARDED_AND_ACKNOWLEDGED) {
                return true;
            }
            if (ecmState == EcmState.ECM_STATE_NOT_GUARDED) {
                return false;
            }

            // Otherwise, lazily decide whether the app is considered guarded.
            InstallSourceInfo installSource;
            try {
                installSource = mContext.createContextAsUser(UserHandle.of(userId), 0)
                        .getPackageManager()
                        .getInstallSourceInfo(packageName);
            } catch (NameNotFoundException e) {
                Log.w(LOG_TAG, "Package not found: " + packageName);
                return false;
            }

            // These install sources are always considered dangerous.
            // PackageInstallers that are trusted can use these as a signal that the
            // packages they've installed aren't as trusted as themselves.
            int packageSource = installSource.getPackageSource();
            if (packageSource == PackageInstaller.PACKAGE_SOURCE_LOCAL_FILE
                    || packageSource == PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE) {
                return true;
            }
            String installingPackageName = installSource.getInstallingPackageName();
            ApplicationInfo installingApplicationInfo =
                    getApplicationInfoAsUser(installingPackageName, userId);

            // ECM doesn't consider a transitive chain of trust for install sources.
            // If this package hasn't been explicitly handled by this point
            // then it is exempt from ECM if the immediate parent is a trusted installer
            return !(trustPackagesInstalledViaNonAllowlistedInstallers()
                    || isPackagePreinstalled(installingApplicationInfo)
                    || isAllowlistedInstaller(installingPackageName));
        }

        private boolean isSettingEcmGuardedForPackage(@NonNull String settingIdentifier,
                @NonNull String packageName, @UserIdInt int userId) throws NameNotFoundException {
            if (!mPerPackageProtectedSettings.contains(settingIdentifier)) {
                return false;
            }
            return isPackageEcmGuarded(packageName, userId);
        }

        private boolean isAllowlistedPackage(String packageName) {
            return isPackageSignedWithAnyOf(packageName,
                    mTrustedPackageCertDigests.get(packageName));
        }

        private boolean isAllowlistedInstaller(String packageName) {
            return isPackageSignedWithAnyOf(packageName,
                    mTrustedInstallerCertDigests.get(packageName));
        }

        private boolean isPackageSignedWithAnyOf(String packageName, List<byte[]> certDigests) {
            if (packageName != null && certDigests != null) {
                for (int i = 0, count = certDigests.size(); i < count; i++) {
                    byte[] trustedCertDigest = certDigests.get(i);
                    if (mPackageManager.hasSigningCertificate(packageName, trustedCertDigest,
                            PackageManager.CERT_INPUT_SHA256)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * @return {@code true} if zero {@code <enhanced-confirmation-trusted-installer>} entries
         * are defined in {@code frameworks/base/data/etc/enhanced-confirmation.xml}; in this case,
         * we treat all installers as trusted.
         */
        private boolean trustPackagesInstalledViaNonAllowlistedInstallers() {
            return mTrustedInstallerCertDigests.isEmpty();
        }

        private boolean isPackagePreinstalled(@Nullable ApplicationInfo applicationInfo) {
            if (applicationInfo == null) {
                return false;
            }
            return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        }

        @SuppressLint("WrongConstant")
        private void setAppEcmState(@NonNull String packageName, @EcmState int ecmState,
                @UserIdInt int userId) throws NameNotFoundException {
            int packageUid = getPackageUid(mPackageManager, packageName, userId);
            final long identityToken = Binder.clearCallingIdentity();
            try {
                mAppOpsManager.setMode(AppOpsManager.OPSTR_ACCESS_RESTRICTED_SETTINGS, packageUid,
                        packageName, ecmState);
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        }

        private @EcmState int getAppEcmState(@NonNull String packageName, @UserIdInt int userId)
                throws NameNotFoundException {
            int packageUid = getPackageUid(mPackageManager, packageName, userId);
            final long identityToken = Binder.clearCallingIdentity();
            try {
                return mAppOpsManager.noteOpNoThrow(AppOpsManager.OPSTR_ACCESS_RESTRICTED_SETTINGS,
                        packageUid, packageName, mAttributionTag, /* message */ null);
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        }

        private boolean isSettingEcmProtected(@NonNull String settingIdentifier) {
            if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                    || mPackageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
                return false;
            }

            if (mPerPackageProtectedSettings.contains(settingIdentifier)) {
                return true;
            }
            if (mUntrustedCallRestrictedSettings.contains(settingIdentifier)) {
                return true;
            }
            // TODO(b/310218979): Add role selections as protected settings
            return false;
        }

        // Generate a global protection reason for why the setting may be blocked. Note, this
        // method will result in a metric being logged, representing a blocked/allowed setting
        private String getGlobalProtectionReason(@NonNull String settingIdentifier,
                @NonNull String packageName, @UserIdInt int userId) {
            if (!mUntrustedCallRestrictedSettings.contains(settingIdentifier)) {
                return null;
            }
            if (mCallTracker == null) {
                return null;
            }
            String reason = null;
            if (mCallTracker.isUntrustedCallOngoing()) {
                if (!AppOpsManager.OPSTR_BIND_ACCESSIBILITY_SERVICE.equals(settingIdentifier)) {
                    reason = REASON_PHONE_STATE;
                }
                if (!isAccessibilityTool(packageName, userId)) {
                    reason = REASON_PHONE_STATE;
                }
            }
            mCallTracker.onEcmInteraction(packageName, userId, settingIdentifier, reason == null);

            return reason;
        }

        private boolean isAccessibilityTool(@NonNull String packageName, @UserIdInt int userId) {
            AccessibilityManager am;
            synchronized (mUserAccessibilityManagers) {
                if (!mUserAccessibilityManagers.containsKey(userId)) {
                    Context userContext =
                            getContext().createContextAsUser(UserHandle.of(userId), 0);
                    mUserAccessibilityManagers.put(userId, userContext.getSystemService(
                            AccessibilityManager.class));
                }
                am = mUserAccessibilityManagers.get(userId);
            }
            List<AccessibilityServiceInfo> infos = am.getInstalledAccessibilityServiceList();
            for (int i = 0; i < infos.size(); i++) {
                AccessibilityServiceInfo info = infos.get(i);
                String servicePackageName = null;
                if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null) {
                    servicePackageName = info.getResolveInfo().serviceInfo.packageName;
                }
                if (packageName.equals(servicePackageName)) {
                    return info.isAccessibilityTool();
                }
            }
            return false;
        }

        @Nullable
        private ApplicationInfo getApplicationInfoAsUser(@Nullable String packageName,
                @UserIdInt int userId) {
            if (packageName == null) {
                Log.w(LOG_TAG, "The packageName should not be null.");
                return null;
            }
            try {
                return mPackageManager.getApplicationInfoAsUser(packageName, /* flags */ 0,
                        UserHandle.of(userId));
            } catch (NameNotFoundException e) {
                Log.w(LOG_TAG, "Package not found: " + packageName, e);
                return null;
            }
        }
    }

    private class CallTracker {
        // The time we will remember an untrusted call
        private static final long UNTRUSTED_CALL_STORAGE_TIME_MS = TimeUnit.HOURS.toMillis(1);
        // The minimum time that must pass between individual logs of the same call, uid, trusted
        // status, and allowed setting.
        private static final long MAX_LOGGING_FREQUENCY_MS = TimeUnit.SECONDS.toMillis(30);
        // A map of call ID to ongoing or recently removed calls. Concurrent because
        // additions/removals happen on background threads, but queries on main thread.
        private final Map<String, TrackedCall> mCalls = new ConcurrentHashMap<>();

        // A cache of hashed callers, uids, trusted status, and allowed status. Ensures that we
        // do not log the same interaction too many times
        private final Map<Integer, Long> mLogCache = new ConcurrentHashMap<>();

        private class TrackedCall {
            public @CallType Integer callType;
            public String caller;

            public long startTime = SystemClock.elapsedRealtime();

            public long endTime = -1;

            public boolean incoming;

            public boolean blockedDuringCall = false;

            public boolean ecmInteractionDuringCall = false;

            public boolean isFinished() {
                return endTime > 0;
            }

            TrackedCall(@NonNull Call call) {
                caller = getPhoneNumber(call);
                if (caller == null) {
                    caller = getDisplayName(call);
                }
                callType = getCallType(call);
                incoming = call.getDetails().getCallDirection() == Call.Details.DIRECTION_INCOMING;
            }
        }

        private Context mContext;
        private TelephonyManager mTelephonyManager;
        private PackageManager mPackageManager;

        CallTracker(Context context) {
            mContext = context;
            mTelephonyManager = context.getSystemService(TelephonyManager.class);
            mPackageManager = context.getPackageManager();
        }

        public void addCall(@NonNull Call call) {
            if (call.getDetails() == null) {
                return;
            }
            pruneOldFinishedCalls();
            pruneOldLogs();
            mCalls.put(call.getDetails().getId(), new TrackedCall(call));
        }

        public void endCall(@NonNull String callId) {
            TrackedCall trackedCall = mCalls.get(callId);
            if (trackedCall == null) {
                // TODO b/379941144: Capture a bug report whenever this happens.
                return;
            }
            if (trackedCall.isFinished()) {
                return;
            }
            if (!Flags.unknownCallSettingBlockedLoggingEnabled()) {
                mCalls.remove(callId);
                return;
            }

            trackedCall.endTime = SystemClock.elapsedRealtime();
            if (trackedCall.callType != CALL_TYPE_UNTRUSTED) {
                // We only hang onto a finished call if the call was untrusted
                mCalls.remove(callId);
            }

            if (trackedCall.ecmInteractionDuringCall) {
                long duration = TimeUnit.MILLISECONDS.toSeconds(
                        trackedCall.endTime - trackedCall.startTime);
                int durationInt =  (int) Math.min(duration, Integer.MAX_VALUE);
                PermissionControllerStatsLog.write(CALL_WITH_ECM_INTERACTION_REPORTED,
                        trackedCall.blockedDuringCall, durationInt);
            }

            pruneOldFinishedCalls();
            pruneOldLogs();
        }

        public void endAllCalls() {
            for (String callId: mCalls.keySet()) {
                endCall(callId);
            }
        }

        public void onEcmInteraction(@NonNull String packageName, int userId,
                @NonNull String settingIdentifier, boolean allowed) {
            if (!Flags.unknownCallSettingBlockedLoggingEnabled()) {
                return;
            }

            boolean hasOngoingCall = false;
            for (TrackedCall current: mCalls.values()) {
                if (current.isFinished()) {
                    // We only care about ongoing calls
                    continue;
                }
                hasOngoingCall = true;
                // Mark that the current call had a setting interaction during it
                current.ecmInteractionDuringCall = true;
                current.blockedDuringCall = !allowed;
                logInCallRestrictionEvent(packageName, userId, settingIdentifier, allowed, current);
            }
            if (!hasOngoingCall) {
                logInCallRestrictionEvent(packageName, userId, settingIdentifier, allowed, null);
            }

        }

        private @CallType int getCallType(@NonNull Call call) {
            String number = getPhoneNumber(call);
            try {
                if (number != null && mTelephonyManager.isEmergencyNumber(number)) {
                    return CALL_TYPE_EMERGENCY;
                }
            } catch (RuntimeException e) {
                // If either of these are thrown, the telephony service is not available on the
                // current device, either because the device lacks telephony calling, or the
                // telephony service is unavailable.
            }
            UserHandle user = mContext.getUser();
            if (android.telecom.flags.Flags.callDetailsGetAssociatedUserApi2()) {
                user = call.getDetails().getAssociatedUser();
            }
            if (number != null) {
                return hasContactWithPhoneNumber(number, user)
                        ? CALL_TYPE_TRUSTED : CALL_TYPE_UNTRUSTED;
            } else {
                return hasContactWithDisplayName(getDisplayName(call), user)
                        ? CALL_TYPE_TRUSTED : CALL_TYPE_UNTRUSTED;
            }
        }

        private static String getPhoneNumber(@NonNull Call call) {
            Uri handle = call.getDetails().getHandle();
            if (handle == null || handle.getScheme() == null) {
                return null;
            }
            if (!handle.getScheme().equals(PhoneAccount.SCHEME_TEL)) {
                return null;
            }
            return handle.getSchemeSpecificPart();
        }

        private static String getDisplayName(@NonNull Call call) {
            return call.getDetails().getCallerDisplayName();
        }

        private boolean hasContactWithPhoneNumber(@Nullable String phoneNumber, UserHandle user) {
            if (phoneNumber == null) {
                return false;
            }
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            String[] projection = new String[]{
                    PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup._ID
            };
            try (Cursor res = getUserContentResolver(user).query(uri, projection, null, null)) {
                return res != null && res.getCount() > 0;
            }
        }

        private boolean hasContactWithDisplayName(@Nullable String displayName, UserHandle user) {
            if (displayName == null) {
                return false;
            }
            Uri uri = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]{PhoneLookup._ID};
            String selection = StructuredName.DISPLAY_NAME + " = ?";
            String[] selectionArgs = new String[]{displayName};
            try (Cursor res = getUserContentResolver(user)
                         .query(uri, projection, selection, selectionArgs, null)) {
                return res != null && res.getCount() > 0;
            }
        }

        private ContentResolver getUserContentResolver(UserHandle user) {
            return mContext.createContextAsUser(user, 0).getContentResolver();
        }

        private TrackedCall getOngoingCallOfType(@CallType int callType) {
            for (TrackedCall call : mCalls.values()) {
                if (!call.isFinished() && call.callType == callType) {
                    return call;
                }
            }
            return null;
        }

        public boolean isUntrustedCallOngoing() {
            if (!Flags.unknownCallPackageInstallBlockingEnabled()) {
                return false;
            }

            if (getOngoingCallOfType(CALL_TYPE_EMERGENCY) != null) {
                // If we have an emergency call, return false always.
                return false;
            }
            return getOngoingCallOfType(CALL_TYPE_UNTRUSTED) != null;
        }

        void pruneOldFinishedCalls() {
            if (!Flags.unknownCallSettingBlockedLoggingEnabled()) {
                return;
            }
            long cutoff = SystemClock.elapsedRealtime() - UNTRUSTED_CALL_STORAGE_TIME_MS;
            mCalls.entrySet().removeIf(
                    e -> e.getValue().isFinished() && e.getValue().endTime < cutoff);
        }

        void pruneOldLogs() {
            if (!Flags.unknownCallSettingBlockedLoggingEnabled()) {
                return;
            }
            long cutoff = SystemClock.elapsedRealtime() - MAX_LOGGING_FREQUENCY_MS;
            mLogCache.entrySet().removeIf(e -> e.getValue() < cutoff);
        }

        private void logInCallRestrictionEvent(@NonNull String packageName, int userId,
                @NonNull String settingIdentifier, boolean allowed, @Nullable TrackedCall call) {
            if (!Flags.unknownCallSettingBlockedLoggingEnabled()) {
                return;
            }

            if (!mUntrustedCallRestrictedSettings.contains(settingIdentifier)) {
                return;
            }

            int uid;
            try {
                uid = mPackageManager.getPackageUid(packageName, userId);
            } catch (NameNotFoundException e) {
                return;
            }

            boolean callInProgress = call != null && !call.isFinished();
            boolean trusted = true;
            boolean incoming = false;
            boolean callBackAfterBlock = false;
            if (callInProgress) {
                trusted = call.callType != CALL_TYPE_UNTRUSTED;
                incoming = call.incoming;

                // Look for a previous call from the same caller, that had a blocked ecm interaction
                for (TrackedCall otherCall : mCalls.values()) {
                    if (!otherCall.isFinished()) {
                        continue;
                    }
                    if (!Objects.equals(otherCall.caller, call.caller)) {
                        continue;
                    }
                    if (!otherCall.blockedDuringCall) {
                        continue;
                    }
                    callBackAfterBlock = true;
                }
            }

            String caller = callInProgress ? call.caller : null;
            int logHash = Objects.hash(caller, uid, settingIdentifier, allowed, trusted);
            Long lastLogTime = mLogCache.get(logHash);
            long now = SystemClock.elapsedRealtime();
            long cutoff = now - MAX_LOGGING_FREQUENCY_MS;
            if (lastLogTime != null && lastLogTime > cutoff) {
                return;
            }
            mLogCache.put(logHash, now);

            PermissionControllerStatsLog.write(ECM_RESTRICTION_QUERY_IN_CALL_REPORTED, uid,
                    settingIdentifier, allowed, callInProgress, incoming, trusted,
                    callBackAfterBlock);
        }
    }
}
