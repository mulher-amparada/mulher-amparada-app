/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.role.controller.model;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.ecm.EnhancedConfirmationManager;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.SharedLibraryInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.permission.flags.Flags;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseBooleanArray;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;

import com.android.modules.utils.build.SdkLevel;
import com.android.role.controller.util.CollectionUtils;
import com.android.role.controller.util.IntentCompat;
import com.android.role.controller.util.PackageUtils;
import com.android.role.controller.util.RoleFlags;
import com.android.role.controller.util.RoleManagerCompat;
import com.android.role.controller.util.SignedPackageUtils;
import com.android.role.controller.util.UserUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Specifies a role and its properties.
 * <p>
 * A role is a unique name within the system associated with certain privileges. There can be
 * multiple applications qualifying for a role, but only a subset of them can become role holders.
 * To qualify for a role, an application must meet certain requirements, including defining certain
 * components in its manifest. Then the application will need user consent to become the role
 * holder.
 * <p>
 * Upon becoming a role holder, the application may be granted certain permissions, have certain
 * app ops set to certain modes and certain {@code Activity} components configured as preferred for
 * certain {@code Intent} actions. When an application loses its role, these privileges will also be
 * revoked.
 *
 * @see android.app.role.RoleManager
 */
public class Role {

    private static final String LOG_TAG = Role.class.getSimpleName();

    private static final boolean DEBUG = false;

    private static final String PACKAGE_NAME_ANDROID_SYSTEM = "android";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            EXCLUSIVITY_NONE,
            EXCLUSIVITY_USER,
            EXCLUSIVITY_PROFILE_GROUP
    })
    public @interface Exclusivity {}

    /**
     * Does not enforce any exclusivity, which means multiple apps may hold this role in a user.
     */
    public static final int EXCLUSIVITY_NONE = 0;

    /** Enforces exclusivity within one user. */
    public static final int EXCLUSIVITY_USER = 1;

    /**
     * Enforces exclusivity across all users (including profile users) in the same profile group.
     */
    public static final int EXCLUSIVITY_PROFILE_GROUP = 2;

    /** Set of valid exclusivity values. */
    private static final SparseBooleanArray sExclusivityValues = new SparseBooleanArray();
    static {
        sExclusivityValues.put(EXCLUSIVITY_NONE, true);
        sExclusivityValues.put(EXCLUSIVITY_USER, true);
        sExclusivityValues.put(EXCLUSIVITY_PROFILE_GROUP,
                RoleFlags.isProfileGroupExclusivityAvailable());
    }

    /**
     * The name of this role. Must be unique.
     */
    @NonNull
    private final String mName;

    /**
     * Whether this role allows bypassing role holder qualification.
     */
    private final boolean mAllowBypassingQualification;

    /**
     * The behavior of this role.
     */
    @Nullable
    private final RoleBehavior mBehavior;

    @Nullable
    private final String mDefaultHoldersResourceName;

    /**
     * The string resource for the description of this role.
     */
    @StringRes
    private final int mDescriptionResource;

    /**
     * The exclusivity of this role, i.e. whether this role allows multiple holders, or allows at
     * most one holder within a user or a profile group.
     */
    private final int mExclusivity;

    /**
     * Whether this role should fall back to the default holder.
     */
    private final boolean mFallBackToDefaultHolder;

    /**
     * The feature flag for this app op to be granted, or {@code null} if none.
     */
    @Nullable
    private final Supplier<Boolean> mFeatureFlag;

    /**
     * The string resource for the label of this role.
     */
    @StringRes
    private final int mLabelResource;

    /**
     * The maximum SDK version for this role to be available.
     */
    private final int mMaxSdkVersion;

    /**
     * The minimum SDK version for this role to be available.
     */
    private final int mMinSdkVersion;

    /**
     * Whether this role should only grant privileges when a role holder is actively added.
     */
    private final boolean mOnlyGrantWhenAdded;

    /**
     * Whether this role should override user's choice about privileges when granting.
     */
    private final boolean mOverrideUserWhenGranting;

    /**
     * The string resource for the request description of this role, shown below the selected app in
     * the request role dialog.
     */
    @StringRes
    private final int mRequestDescriptionResource;

    /**
     * The string resource for the request title of this role, shown as the title of the request
     * role dialog.
     */
    @StringRes
    private final int mRequestTitleResource;

    /**
     * Whether this role is requestable by applications with
     * {@link android.app.role.RoleManager#createRequestRoleIntent(String)}.
     */
    private final boolean mRequestable;

    /**
     * The string resource for search keywords of this role, in addition to the label of this role,
     * if it's non-zero.
     */
    @StringRes
    private final int mSearchKeywordsResource;

    /**
     * The string resource for the short label of this role, currently used when in a list of roles.
     */
    @StringRes
    private final int mShortLabelResource;

    /**
     * Whether the UI for this role will show the "None" item. Only valid if this role is
     * {@link #isExclusive()}, and {@link #getFallbackHolder(Context)} should
     * also return empty to allow actually selecting "None".
     */
    private final boolean mShowNone;

    /**
     * Whether this role is static, i.e. the role will always be assigned to its default holders.
     */
    private final boolean mStatic;

    /**
     * Whether this role only accepts system apps as its holders.
     */
    private final boolean mSystemOnly;

    /**
     * Whether this role is visible to user.
     */
    private final boolean mVisible;

    /**
     * The requirements for an application to qualify for this role.
     */
    @NonNull
    private final List<Requirement> mRequirements;

    /**
     * The permissions to be granted by this role.
     */
    @NonNull
    private final List<Permission> mPermissions;

    /**
     * The app op permissions to be granted by this role.
     */
    @NonNull
    private final List<Permission> mAppOpPermissions;

    /**
     * The app ops to be set to allowed by this role.
     */
    @NonNull
    private final List<AppOp> mAppOps;

    /**
     * The set of preferred {@code Activity} configurations to be configured by this role.
     */
    @NonNull
    private final List<PreferredActivity> mPreferredActivities;

    @Nullable
    private final String mUiBehaviorName;

    public Role(@NonNull String name, boolean allowBypassingQualification,
            @Nullable RoleBehavior behavior, @Nullable String defaultHoldersResourceName,
            @StringRes int descriptionResource, @Exclusivity int exclusivity,
            boolean fallBackToDefaultHolder, @Nullable Supplier<Boolean> featureFlag,
            @StringRes int labelResource, int maxSdkVersion, int minSdkVersion,
            boolean onlyGrantWhenAdded, boolean overrideUserWhenGranting,
            @StringRes int requestDescriptionResource, @StringRes int requestTitleResource,
            boolean requestable, @StringRes int searchKeywordsResource,
            @StringRes int shortLabelResource, boolean showNone, boolean statik, boolean systemOnly,
            boolean visible, @NonNull List<Requirement> requirements,
            @NonNull List<Permission> permissions, @NonNull List<Permission> appOpPermissions,
            @NonNull List<AppOp> appOps, @NonNull List<PreferredActivity> preferredActivities,
            @Nullable String uiBehaviorName) {
        mName = name;
        mAllowBypassingQualification = allowBypassingQualification;
        mBehavior = behavior;
        mDefaultHoldersResourceName = defaultHoldersResourceName;
        mDescriptionResource = descriptionResource;
        mExclusivity = exclusivity;
        mFallBackToDefaultHolder = fallBackToDefaultHolder;
        mFeatureFlag = featureFlag;
        mLabelResource = labelResource;
        mMaxSdkVersion = maxSdkVersion;
        mMinSdkVersion = minSdkVersion;
        mOnlyGrantWhenAdded = onlyGrantWhenAdded;
        mOverrideUserWhenGranting = overrideUserWhenGranting;
        mRequestDescriptionResource = requestDescriptionResource;
        mRequestTitleResource = requestTitleResource;
        mRequestable = requestable;
        mSearchKeywordsResource = searchKeywordsResource;
        mShortLabelResource = shortLabelResource;
        mShowNone = showNone;
        mStatic = statik;
        mSystemOnly = systemOnly;
        mVisible = visible;
        mRequirements = requirements;
        mPermissions = permissions;
        mAppOpPermissions = appOpPermissions;
        mAppOps = appOps;
        mPreferredActivities = preferredActivities;
        mUiBehaviorName = uiBehaviorName;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @Nullable
    public RoleBehavior getBehavior() {
        return mBehavior;
    }

    @StringRes
    public int getDescriptionResource() {
        return mDescriptionResource;
    }

    public boolean isExclusive() {
        return  getExclusivity() != EXCLUSIVITY_NONE;
    }

    @Exclusivity
    public int getExclusivity() {
        if (com.android.permission.flags.Flags.crossUserRoleEnabled() && mBehavior != null) {
            Integer exclusivity = mBehavior.getExclusivity();
            if (exclusivity != null) {
                if (!sExclusivityValues.get(exclusivity)) {
                    throw new IllegalArgumentException(
                        "Role " + mName + " has invalid exclusivity: "
                            + exclusivity);
                }
                if (mShowNone && exclusivity == EXCLUSIVITY_NONE) {
                    throw new IllegalArgumentException(
                        "Role " + mName + " cannot be non-exclusive when showNone is true: "
                            + exclusivity);
                }
                if (!mPreferredActivities.isEmpty() && exclusivity == EXCLUSIVITY_PROFILE_GROUP) {
                    throw new IllegalArgumentException(
                        "Role " + mName + " cannot have preferred activities when exclusivity is "
                            + "profileGroup");
                }
                return exclusivity;
            }
        }
        return mExclusivity;
    }

    @Nullable
    public Supplier<Boolean> getFeatureFlag() {
        return mFeatureFlag;
    }

    @StringRes
    public int getLabelResource() {
        return mLabelResource;
    }

    @StringRes
    public int getRequestDescriptionResource() {
        return mRequestDescriptionResource;
    }

    @StringRes
    public int getRequestTitleResource() {
        return mRequestTitleResource;
    }

    public boolean isRequestable() {
        return mRequestable;
    }

    @StringRes
    public int getSearchKeywordsResource() {
        return mSearchKeywordsResource;
    }

    @StringRes
    public int getShortLabelResource() {
        return mShortLabelResource;
    }

    /**
     * @see #mOnlyGrantWhenAdded
     */
    public boolean shouldOnlyGrantWhenAdded() {
        return mOnlyGrantWhenAdded;
    }

    /**
     * @see #mOverrideUserWhenGranting
     */
    public boolean shouldOverrideUserWhenGranting() {
        return mOverrideUserWhenGranting;
    }

    /**
     * @see #mShowNone
     */
    public boolean shouldShowNone() {
        return mShowNone;
    }

    public boolean isVisible() {
        return mVisible;
    }

    @NonNull
    public List<Requirement> getRequirements() {
        return mRequirements;
    }

    @NonNull
    public List<Permission> getPermissions() {
        return mPermissions;
    }

    @NonNull
    public List<Permission> getAppOpPermissions() {
        return mAppOpPermissions;
    }

    @NonNull
    public List<AppOp> getAppOps() {
        return mAppOps;
    }

    @NonNull
    public List<PreferredActivity> getPreferredActivities() {
        return mPreferredActivities;
    }

    @Nullable
    public String getUiBehaviorName() {
        return mUiBehaviorName;
    }

    /**
     * Callback when this role is added to the system for the first time.
     *
     * @param user the user to add the role for
     * @param context the {@code Context} to retrieve system services
     */
    public void onRoleAddedAsUser(@NonNull UserHandle user, @NonNull Context context) {
        if (mBehavior != null) {
            mBehavior.onRoleAddedAsUser(this, user, context);
        }
    }

    /**
     * Check whether this role is available.
     *
     * @param user the user to check for
     * @param context the {@code Context} to retrieve system services
     *
     * @return whether this role is available.
     */
    public boolean isAvailableAsUser(@NonNull UserHandle user, @NonNull Context context) {
        if (!isAvailableByFeatureFlagAndSdkVersion()) {
            return false;
        }
        if (getExclusivity() == EXCLUSIVITY_PROFILE_GROUP) {
            if (UserUtils.isPrivateProfile(user, context)) {
                return false;
            }

            // A profile group exclusive role may only be available in a profile when it's
            // available in the profile parent.
            UserHandle profileParent = UserUtils.getProfileParentOrSelf(user, context);
            if (!Objects.equals(user, profileParent)
                    && !isAvailableAsUser(profileParent, context)) {
                return false;
            }
        }
        if (mBehavior != null) {
            return mBehavior.isAvailableAsUser(this, user, context);
        }
        return true;
    }

    /**
     * Check whether this role is available based on SDK version.
     *
     * @return whether this role is available based on SDK version
     */
    @VisibleForTesting
    public boolean isAvailableByFeatureFlagAndSdkVersion() {
        if (mFeatureFlag != null && !mFeatureFlag.get()) {
            return false;
        }
        return (Build.VERSION.SDK_INT >= mMinSdkVersion
                // Workaround to match the value 36 for B in roles.xml before SDK finalization.
                || (mMinSdkVersion == 36 && SdkLevel.isAtLeastB()))
                && Build.VERSION.SDK_INT <= mMaxSdkVersion;
    }

    /**
     * Check whether this role is static, which may change due to bypassing qualification.
     *
     * @param context the {@code Context} to retrieve system services
     * @return whether this role is static
     */
    public boolean isStatic(@NonNull Context context) {
        return mStatic && !isBypassingQualification(context);
    }

    /**
     * Get the default holders of this role, which will be added when the role is added for the
     * first time.
     *
     * @param user the user of the role
     * @param context the {@code Context} to retrieve system services
     * @return the list of package names of the default holders
     */
    @NonNull
    public List<String> getDefaultHoldersAsUser(@NonNull UserHandle user,
            @NonNull Context context) {
        // Do not allow default role holder for non-active user if the role is exclusive to profile
        // group
        if (isNonActiveUserForProfileGroupExclusiveRole(user, context)) {
            return Collections.emptyList();
        }

        if (mBehavior != null) {
            List<String> defaultHolders = mBehavior.getDefaultHoldersAsUser(this, user, context);
            if (defaultHolders != null) {
                return defaultHolders;
            }
        }

        if (mDefaultHoldersResourceName == null) {
            return Collections.emptyList();
        }

        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier(mDefaultHoldersResourceName, "string", "android");
        if (resourceId == 0) {
            Log.w(LOG_TAG, "Cannot find resource for default holder: "
                    + mDefaultHoldersResourceName);
            return Collections.emptyList();
        }

        String defaultHolders;
        try {
            defaultHolders = resources.getString(resourceId);
        } catch (Resources.NotFoundException e) {
            Log.w(LOG_TAG, "Cannot get resource for default holder: " + mDefaultHoldersResourceName,
                    e);
            return Collections.emptyList();
        }
        if (TextUtils.isEmpty(defaultHolders)) {
            return Collections.emptyList();
        }

        if (isExclusive()) {
            return CollectionUtils.singletonOrEmpty(
                    SignedPackageUtils.getPackageNameAsUser(defaultHolders, user, context));
        } else {
            return SignedPackageUtils.getPackageNamesAsUser(defaultHolders, user, context);
        }
    }

    /**
     * Get the fallback holder of this role, which will be added whenever there are no role holders.
     * <p>
     * Should return {@code null} if this role {@link #mShowNone shows a "None" item}.
     *
     * @param user the user of the role
     * @param context the {@code Context} to retrieve system services
     * @return the package name of the fallback holder, or {@code null} if none
     */
    @Nullable
    public String getFallbackHolderAsUser(@NonNull UserHandle user, @NonNull Context context) {
        if (!RoleManagerCompat.isRoleFallbackEnabledAsUser(this, user, context)) {
            return null;
        }
        // Do not fall back for non-active user if the role is exclusive to profile group
        if (isNonActiveUserForProfileGroupExclusiveRole(user, context)) {
            return null;
        }
        if (mFallBackToDefaultHolder) {
            return CollectionUtils.firstOrNull(getDefaultHoldersAsUser(user, context));
        }
        if (mBehavior != null) {
            return mBehavior.getFallbackHolderAsUser(this, user, context);
        }
        return null;
    }

    private boolean isNonActiveUserForProfileGroupExclusiveRole(@NonNull UserHandle user,
            @NonNull Context context) {
        if (RoleFlags.isProfileGroupExclusivityAvailable()
                && getExclusivity() == Role.EXCLUSIVITY_PROFILE_GROUP) {
            Context userContext = UserUtils.getUserContext(context, user);
            RoleManager userRoleManager = userContext.getSystemService(RoleManager.class);
            return !Objects.equals(userRoleManager.getActiveUserForRole(mName), user);
        }
        return false;
    }

    /**
     * Check whether this role is allowed to bypass qualification, if enabled globally.
     *
     * @param context the {@code Context} to retrieve system services
     *
     * @return whether this role is allowed to bypass qualification
     */
    public boolean shouldAllowBypassingQualification(@NonNull Context context) {
        if (mBehavior != null) {
            Boolean allowBypassingQualification = mBehavior.shouldAllowBypassingQualification(this,
                    context);
            if (allowBypassingQualification != null) {
                return allowBypassingQualification;
            }
        }
        return mAllowBypassingQualification;
    }

    private boolean isBypassingQualification(@NonNull Context context) {
        RoleManager roleManager = context.getSystemService(RoleManager.class);
        return shouldAllowBypassingQualification(context)
                && RoleManagerCompat.isBypassingRoleQualification(roleManager);
    }

    /**
     * Check whether a package is qualified for this role, i.e. whether it contains all the required
     * components (plus meeting some other general restrictions).
     *
     * @param packageName the package name to check for
     * @param user the user to check for
     * @param context the {@code Context} to retrieve system services
     *
     * @return whether the package is qualified for a role
     */
    public boolean isPackageQualifiedAsUser(@NonNull String packageName, @NonNull UserHandle user,
            @NonNull Context context) {
        if (isBypassingQualification(context)) {
            if (mBehavior != null) {
                boolean isPackageAllowedToBypassQualification =
                        mBehavior.isPackageAllowedToBypassQualificationAsUser(packageName, user,
                                context);
                return isPackageAllowedToBypassQualification;
            }
            return true;
        }

        PackageInfo packageInfo = getPackageInfoAsUser(packageName, hasRequiredUsesPermission(),
                null, user, context);
        if (packageInfo == null) {
            return false;
        }
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;

        if (!isPackageMinimallyQualifiedAsUser(applicationInfo, user, context)) {
            return false;
        }

        if (mBehavior != null) {
            Boolean isPackageQualified = mBehavior.isPackageQualifiedAsUser(this, packageName,
                    user, context);
            if (isPackageQualified != null) {
                return isPackageQualified;
            }
        }

        int requirementsSize = mRequirements.size();
        for (int i = 0; i < requirementsSize; i++) {
            Requirement requirement = mRequirements.get(i);

            if (!requirement.isRequired(applicationInfo)) {
                continue;
            }

            if (!requirement.isQualifiedAsUser(packageInfo, user, context)) {
                Log.i(LOG_TAG, packageName + " not qualified for " + mName + " due to missing "
                        + requirement);
                return false;
            }
        }

        if (mStatic && !getDefaultHoldersAsUser(user, context).contains(packageName)) {
            return false;
        }

        return true;
    }

    private boolean hasRequiredUsesPermission() {
        for (int i = 0; i < mRequirements.size(); i++) {
            Requirement requirement = mRequirements.get(i);

            if (requirement instanceof RequiredUsesPermission requiredUsesPermission) {
                if (requiredUsesPermission.isAvailable()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the list of packages that are qualified for this role, i.e. packages containing all the
     * required components (plus meeting some other general restrictions).
     *
     * @param user the user to get the qualifying packages.
     * @param context the {@code Context} to retrieve system services
     *
     * @return the list of packages that are qualified for this role
     */
    @NonNull
    public List<String> getQualifyingPackagesAsUser(@NonNull UserHandle user,
            @NonNull Context context) {
        List<String> qualifyingPackages = null;

        if (mBehavior != null) {
            qualifyingPackages = mBehavior.getQualifyingPackagesAsUser(this, user, context);
        }

        ArrayMap<String, PackageInfo> packageInfoCache = new ArrayMap<>();
        if (qualifyingPackages == null) {
            // Gather all the packages with all the required components they have by querying each
            // of the required components, instead of spending one binder call to check for each
            // pair of package and required component later, so that we have predictable/better
            // performance.
            // As a special case, if we don't have any required components, but we do have at least
            // one required uses-permission, this will be populated with all installed package names
            // and a {@code null} for set of required components, so that we can reuse the same
            // logic later on.
            ArrayMap<String, ArraySet<RequiredComponent>> packageRequiredComponentsMap =
                    new ArrayMap<>();
            boolean hasRequiredUsesPermission = false;
            boolean hasRequiredComponent = false;
            int requirementsSize = mRequirements.size();
            for (int requirementIndex = 0; requirementIndex < requirementsSize;
                    requirementIndex++) {
                Requirement requirement = mRequirements.get(requirementIndex);

                if (!requirement.isAvailable()) {
                    continue;
                }

                switch (requirement) {
                    case RequiredUsesPermission requiredUsesPermission:
                        hasRequiredUsesPermission = true;
                        break;
                    case RequiredComponent requiredComponent: {
                        hasRequiredComponent = true;
                        // This returns at most one component per package.
                        List<ComponentName> qualifyingComponents =
                                requiredComponent.getQualifyingComponentsAsUser(user, context);
                        int qualifyingComponentsSize = qualifyingComponents.size();
                        for (int qualifyingComponentsIndex = 0;
                                qualifyingComponentsIndex < qualifyingComponentsSize;
                                ++qualifyingComponentsIndex) {
                            ComponentName componentName = qualifyingComponents.get(
                                    qualifyingComponentsIndex);

                            String packageName = componentName.getPackageName();
                            ArraySet<RequiredComponent> packageRequiredComponents =
                                    packageRequiredComponentsMap.get(packageName);
                            if (packageRequiredComponents == null) {
                                packageRequiredComponents = new ArraySet<>();
                                packageRequiredComponentsMap.put(packageName,
                                        packageRequiredComponents);
                            }
                            packageRequiredComponents.add(requiredComponent);
                        }
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unknown requirement: " + requirement);
                }
            }

            if (!hasRequiredComponent && hasRequiredUsesPermission) {
                // If we don't have any required components, but we do have at least one required
                // uses-permission, we'll have to check against all installed packages despite being
                // much slower.
                List<PackageInfo> packageInfos = PackageUtils.getInstalledPackagesAsUser(
                        PackageManager.GET_PERMISSIONS, user, context);
                int packageInfosSize = packageInfos.size();
                for (int packageInfosIndex = 0; packageInfosIndex < packageInfosSize;
                        packageInfosIndex++) {
                    PackageInfo packageInfo = packageInfos.get(packageInfosIndex);

                    if (packageInfo.applicationInfo == null) {
                        continue;
                    }

                    packageInfoCache.put(packageInfo.packageName, packageInfo);
                    packageRequiredComponentsMap.put(packageInfo.packageName, null);
                }
            }

            qualifyingPackages = new ArrayList<>();
            int packageRequiredComponentsMapSize = packageRequiredComponentsMap.size();
            for (int packageRequiredComponentsMapIndex = 0;
                    packageRequiredComponentsMapIndex < packageRequiredComponentsMapSize;
                    packageRequiredComponentsMapIndex++) {
                String packageName = packageRequiredComponentsMap.keyAt(
                        packageRequiredComponentsMapIndex);
                ArraySet<RequiredComponent> packageRequiredComponents =
                        packageRequiredComponentsMap.valueAt(packageRequiredComponentsMapIndex);

                PackageInfo packageInfo = getPackageInfoAsUser(packageName,
                        hasRequiredUsesPermission, packageInfoCache, user, context);
                if (packageInfo == null) {
                    continue;
                }
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;

                boolean isPackageQualified = true;
                for (int requirementIndex = 0; requirementIndex < requirementsSize;
                        requirementIndex++) {
                    Requirement requirement = mRequirements.get(requirementIndex);

                    if (!requirement.isRequired(applicationInfo)) {
                        continue;
                    }

                    if (requirement instanceof RequiredComponent requiredComponent) {
                        if (!CollectionUtils.contains(packageRequiredComponents,
                                requiredComponent)) {
                            isPackageQualified = false;
                            break;
                        }
                    } else {
                        if (!requirement.isQualifiedAsUser(packageInfo, user, context)) {
                            isPackageQualified = false;
                            break;
                        }
                    }
                }

                if (isPackageQualified) {
                    qualifyingPackages.add(packageName);
                }
            }
        }

        for (int i = qualifyingPackages.size() - 1; i >= 0; i--) {
            String packageName = qualifyingPackages.get(i);

            PackageInfo packageInfo = getPackageInfoAsUser(packageName, false, packageInfoCache,
                    user, context);
            if (packageInfo == null) {
                qualifyingPackages.remove(i);
                continue;
            }
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

            if (!isPackageMinimallyQualifiedAsUser(applicationInfo, user, context)) {
                qualifyingPackages.remove(i);
            }
        }

        return qualifyingPackages;
    }

    @NonNull
    private PackageInfo getPackageInfoAsUser(@NonNull String packageName,
            boolean getPermissions, @Nullable ArrayMap<String, PackageInfo> packageInfoCache,
            @NonNull UserHandle user, @NonNull Context context) {
        PackageInfo packageInfo = packageInfoCache != null ? packageInfoCache.get(packageName)
                : null;
        if (packageInfo == null) {
            packageInfo = PackageUtils.getPackageInfoAsUser(packageName,
                    getPermissions ? PackageManager.GET_PERMISSIONS : 0, user, context);
            if (packageInfo == null) {
                Log.w(LOG_TAG, "Cannot get PackageInfo for package: " + packageName + ", user: "
                        + user.getIdentifier());
                return null;
            }
            if (packageInfo.applicationInfo == null) {
                Log.w(LOG_TAG, "Cannot get ApplicationInfo for package: " + packageName + ", user: "
                        + user.getIdentifier());
                return null;
            }
            if (packageInfoCache != null) {
                packageInfoCache.put(packageName, packageInfo);
            }
        }
        return packageInfo;
    }

    private boolean isPackageMinimallyQualifiedAsUser(@NonNull ApplicationInfo applicationInfo,
                                                      @NonNull UserHandle user,
                                                      @NonNull Context context) {
        String packageName = applicationInfo.packageName;
        if (Objects.equals(packageName, PACKAGE_NAME_ANDROID_SYSTEM)) {
            return false;
        }

        if (mSystemOnly && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return false;
        }

        if (!applicationInfo.enabled) {
            return false;
        }

        if (applicationInfo.isInstantApp()) {
            return false;
        }

        PackageManager userPackageManager = UserUtils.getUserContext(context, user)
                .getPackageManager();
        List<SharedLibraryInfo> sharedLibraries = userPackageManager.getDeclaredSharedLibraries(
                packageName, 0);
        final int sharedLibrariesSize = sharedLibraries.size();
        for (int i = 0; i < sharedLibrariesSize; i++) {
            SharedLibraryInfo sharedLibrary = sharedLibraries.get(i);
            if (sharedLibrary.getType() != SharedLibraryInfo.TYPE_DYNAMIC) {
                return false;
            }
        }

        return true;
    }

    /**
     * Grant this role to an application.
     *
     * @param packageName the package name of the application to be granted this role to
     * @param dontKillApp whether this application should not be killed despite changes
     * @param overrideUser whether to override user when granting privileges
     * @param user the user of the application
     * @param context the {@code Context} to retrieve system services
     */
    public void grantAsUser(@NonNull String packageName, boolean dontKillApp,
            boolean overrideUser, @NonNull UserHandle user, @NonNull Context context) {
        boolean permissionOrAppOpChanged = Permissions.grantAsUser(packageName,
                Permissions.filterBySdkVersionAsUser(mPermissions, user, context),
                SdkLevel.isAtLeastS() ? !mSystemOnly : true, overrideUser, true, false, false,
                user, context);

        List<String> appOpPermissionsToGrant =
                Permissions.filterBySdkVersionAsUser(mAppOpPermissions, user, context);
        int appOpPermissionsSize = appOpPermissionsToGrant.size();
        for (int i = 0; i < appOpPermissionsSize; i++) {
            String appOpPermission = appOpPermissionsToGrant.get(i);
            AppOpPermissions.grantAsUser(packageName, appOpPermission, overrideUser, user, context);
        }

        int appOpsSize = mAppOps.size();
        for (int i = 0; i < appOpsSize; i++) {
            AppOp appOp = mAppOps.get(i);
            appOp.grantAsUser(packageName, user, context);
        }

        int preferredActivitiesSize = mPreferredActivities.size();
        for (int i = 0; i < preferredActivitiesSize; i++) {
            PreferredActivity preferredActivity = mPreferredActivities.get(i);
            preferredActivity.configureAsUser(packageName, user, context);
        }

        if (mBehavior != null) {
            mBehavior.grantAsUser(this, packageName, overrideUser, user, context);
        }

        if (!dontKillApp && permissionOrAppOpChanged
                && !Permissions.isRuntimePermissionsSupportedAsUser(packageName, user, context)) {
            killAppAsUser(packageName, user, context);
        }
    }

    /**
     * Revoke this role from an application.
     *
     * @param packageName the package name of the application to be granted this role to
     * @param dontKillApp whether this application should not be killed despite changes
     * @param overrideSystemFixedPermissions whether system-fixed permissions can be revoked
     * @param user the user of the role
     * @param context the {@code Context} to retrieve system services
     */
    public void revokeAsUser(@NonNull String packageName, boolean dontKillApp,
            boolean overrideSystemFixedPermissions, @NonNull UserHandle user,
            @NonNull Context context) {
        Context userContext = UserUtils.getUserContext(context, user);
        RoleManager userRoleManager = userContext.getSystemService(RoleManager.class);
        List<String> otherRoleNames = userRoleManager.getHeldRolesFromController(packageName);
        otherRoleNames.remove(mName);

        List<String> permissionsToRevoke =
                Permissions.filterBySdkVersionAsUser(mPermissions, user, context);
        ArrayMap<String, Role> roles = Roles.get(context);
        int otherRoleNamesSize = otherRoleNames.size();
        for (int i = 0; i < otherRoleNamesSize; i++) {
            String roleName = otherRoleNames.get(i);
            Role role = roles.get(roleName);
            permissionsToRevoke.removeAll(
                    Permissions.filterBySdkVersionAsUser(role.mPermissions, user, context));
        }

        boolean permissionOrAppOpChanged = Permissions.revokeAsUser(packageName,
                permissionsToRevoke, true, false, overrideSystemFixedPermissions, user, context);

        List<String> appOpPermissionsToRevoke = Permissions.filterBySdkVersionAsUser(
                mAppOpPermissions, user, context);
        for (int i = 0; i < otherRoleNamesSize; i++) {
            String roleName = otherRoleNames.get(i);
            Role role = roles.get(roleName);
            appOpPermissionsToRevoke.removeAll(
                    Permissions.filterBySdkVersionAsUser(role.mAppOpPermissions, user, context));
        }
        int appOpPermissionsSize = appOpPermissionsToRevoke.size();
        for (int i = 0; i < appOpPermissionsSize; i++) {
            String appOpPermission = appOpPermissionsToRevoke.get(i);
            AppOpPermissions.revokeAsUser(packageName, appOpPermission, user, context);
        }

        List<AppOp> appOpsToRevoke = new ArrayList<>(mAppOps);
        for (int i = 0; i < otherRoleNamesSize; i++) {
            String roleName = otherRoleNames.get(i);
            Role role = roles.get(roleName);
            appOpsToRevoke.removeAll(role.mAppOps);
        }
        int appOpsSize = appOpsToRevoke.size();
        for (int i = 0; i < appOpsSize; i++) {
            AppOp appOp = appOpsToRevoke.get(i);
            appOp.revokeAsUser(packageName, user, context);
        }

        // TODO: Revoke preferred activities? But this is unnecessary for most roles using it as
        //  they have fallback holders. Moreover, clearing the preferred activity might result in
        //  other system components listening to preferred activity change get notified for the
        //  wrong thing when we are removing a exclusive role holder for adding another.

        if (mBehavior != null) {
            mBehavior.revokeAsUser(this, packageName, user, context);
        }

        if (!dontKillApp && permissionOrAppOpChanged) {
            killAppAsUser(packageName, user, context);
        }
    }

    private void killAppAsUser(@NonNull String packageName, @NonNull UserHandle user,
            @NonNull Context context) {
        if (DEBUG) {
            Log.i(LOG_TAG, "Killing " + packageName + " due to "
                    + Thread.currentThread().getStackTrace()[3].getMethodName()
                    + "(" + mName + ")");
        }
        ApplicationInfo applicationInfo = PackageUtils.getApplicationInfoAsUser(packageName, user,
                context);
        if (applicationInfo == null) {
            Log.w(LOG_TAG, "Cannot get ApplicationInfo for package: " + packageName);
            return;
        }
        ActivityManager activityManager = context.getSystemService(ActivityManager.class);
        activityManager.killUid(applicationInfo.uid, "Permission or app op changed");
    }

    /**
     * Callback when a role holder (other than "none") was added.
     *
     * @param packageName the package name of the role holder
     * @param user the user for the role
     * @param context the {@code Context} to retrieve system services
     */
    public void onHolderAddedAsUser(@NonNull String packageName, @NonNull UserHandle user,
            @NonNull Context context) {
        if (RoleFlags.isProfileGroupExclusivityAvailable()
                && com.android.permission.flags.Flags.crossUserRoleUxBugfixEnabled()
                && getExclusivity() == Role.EXCLUSIVITY_PROFILE_GROUP) {
            UserHandle profileParent = UserUtils.getProfileParentOrSelf(user, context);
            RoleManagerCompat.setRoleFallbackEnabledAsUser(this, true, profileParent, context);
        } else {
            RoleManagerCompat.setRoleFallbackEnabledAsUser(this, true, user, context);
        }
    }

    /**
     * Callback when a role holder (other than "none") was selected in the UI and added
     * successfully.
     *
     * @param packageName the package name of the role holder
     * @param user the user for the role
     * @param context the {@code Context} to retrieve system services
     */
    public void onHolderSelectedAsUser(@NonNull String packageName, @NonNull UserHandle user,
            @NonNull Context context) {
        if (mBehavior != null) {
            mBehavior.onHolderSelectedAsUser(this, packageName, user, context);
        }
    }

    /**
     * Callback when a role holder changed.
     *
     * @param user the user for the role
     * @param context the {@code Context} to retrieve system services
     */
    public void onHolderChangedAsUser(@NonNull UserHandle user,
            @NonNull Context context) {
        if (mBehavior != null) {
            mBehavior.onHolderChangedAsUser(this, user, context);
        }
    }

    /**
     * Callback when the "none" role holder was selected in the UI.
     *
     * @param user the user for the role
     * @param context the {@code Context} to retrieve system services
     */
    public void onNoneHolderSelectedAsUser(@NonNull UserHandle user, @NonNull Context context) {
        RoleManagerCompat.setRoleFallbackEnabledAsUser(this, false, user, context);
        if (RoleFlags.isProfileGroupExclusivityAvailable()
                && getExclusivity() == Role.EXCLUSIVITY_PROFILE_GROUP) {
            RoleManager roleManager = context.getSystemService(RoleManager.class);
            roleManager.setActiveUserForRole(mName, user, 0);
        }
    }

    /**
     * Check whether this role should be visible to user.
     *
     * @param user the user to check for
     * @param context the {@code Context} to retrieve system services
     *
     * @return whether this role should be visible to user
     */
    public boolean isVisibleAsUser(@NonNull UserHandle user, @NonNull Context context) {
        if (mBehavior != null) {
            Boolean isVisibleAsUser = mBehavior.isVisibleAsUser(this, user, context);
            if (isVisibleAsUser != null) {
                if (isVisibleAsUser && mStatic) {
                    throw new IllegalArgumentException("static=\"true\" is invalid for a visible "
                            + "role: " + mName);
                }
                if (isVisibleAsUser && (mDescriptionResource == 0
                        || mLabelResource == 0
                        || mShortLabelResource == 0)) {
                    throw new IllegalArgumentException("description, label, and shortLabel are "
                            + "required for a visible role: " + mName);
                }
                return isVisibleAsUser;
            }
        }
        return isVisible();
    }

    /**
     * Check whether a qualifying application should be visible to user.
     *
     * @param applicationInfo the {@link ApplicationInfo} for the application
     * @param user the user for the application
     * @param context the {@code Context} to retrieve system services
     *
     * @return whether the qualifying application should be visible to user
     */
    public boolean isApplicationVisibleAsUser(@NonNull ApplicationInfo applicationInfo,
            @NonNull UserHandle user,  @NonNull Context context) {
        RoleBehavior behavior = getBehavior();
        if (behavior == null) {
            return true;
        }
        return behavior.isApplicationVisibleAsUser(this, applicationInfo, user, context);
    }

    /**
     * Check whether this role is restricted and return the {@code Intent} for the restriction if it
     * is.
     * <p>
     * If a role is restricted, it is implied that all applications are restricted for the role as
     * well.
     *
     * @param user the user to check for
     * @param context the {@code Context} to retrieve system services
     *
     * @return the {@code Intent} for the restriction if this role is restricted, or {@code null}
     *         otherwise.
     */
    @Nullable
    public Intent getRestrictionIntentAsUser(@NonNull UserHandle user, @NonNull Context context) {
        if (SdkLevel.isAtLeastU() && isExclusive()) {
            boolean crossUserRoleUxBugfixEnabled =
                    com.android.permission.flags.Flags.crossUserRoleUxBugfixEnabled();
            if (crossUserRoleUxBugfixEnabled && getExclusivity() == EXCLUSIVITY_PROFILE_GROUP) {
                DevicePolicyManager devicePolicyManager =
                        context.getSystemService(DevicePolicyManager.class);
                if (!devicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile()) {
                    // For profileGroup exclusive roles users on BYOD are free to choose personal or
                    // work profile app regardless of DISALLOW_CONFIG_DEFAULT_APPS
                    return null;
                }
            }

            // Otherwise if role is profileGroup exclusive check DISALLOW_CONFIG_DEFAULT_APPS for
            // all users
            List<UserHandle> profiles =
                    (crossUserRoleUxBugfixEnabled && getExclusivity() == EXCLUSIVITY_PROFILE_GROUP)
                            ? UserUtils.getUserProfiles(user, context, true)
                            : List.of(user);
            final int profilesSize = profiles.size();
            for (int i = 0; i < profilesSize; i++) {
                UserHandle profile = profiles.get(i);
                UserManager userManager = context.getSystemService(UserManager.class);
                if (userManager.hasUserRestrictionForUser(
                        UserManager.DISALLOW_CONFIG_DEFAULT_APPS, profile)) {
                    return new Intent(Settings.ACTION_SHOW_ADMIN_SUPPORT_DETAILS)
                            .putExtra(
                                    DevicePolicyManager.EXTRA_RESTRICTION,
                                    UserManager.DISALLOW_CONFIG_DEFAULT_APPS)
                            .putExtra(Intent.EXTRA_USER, profile)
                            .putExtra(IntentCompat.EXTRA_USER_ID, profile.getIdentifier());
                }
            }
        }
        return null;
    }

    /**
     * Check whether an application is restricted for this role and return the {@code Intent} for
     * the restriction if it is.
     * <p>
     * If a role is restricted, it is implied that all applications are restricted for the role as
     * well.
     *
     * @param applicationInfo the {@link ApplicationInfo} for the application
     * @param user the user to check for
     * @param context the {@code Context} to retrieve system services
     *
     * @return the {@code Intent} for the restriction if the application is restricted for this
     *         role, or {@code null} otherwise.
     */
    @Nullable
    public Intent getApplicationRestrictionIntentAsUser(@NonNull ApplicationInfo applicationInfo,
            @NonNull UserHandle user, @NonNull Context context) {
        if (SdkLevel.isAtLeastV() && Flags.enhancedConfirmationModeApisEnabled()) {
            Context userContext = UserUtils.getUserContext(context, user);
            EnhancedConfirmationManager userEnhancedConfirmationManager =
                    userContext.getSystemService(EnhancedConfirmationManager.class);
            String packageName = applicationInfo.packageName;
            boolean isRestricted;
            try {
                isRestricted = userEnhancedConfirmationManager.isRestricted(packageName, mName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(LOG_TAG, "Cannot check enhanced confirmation restriction for package: "
                        + packageName, e);
                isRestricted = false;
            }
            if (isRestricted) {
                try {
                    return userEnhancedConfirmationManager.createRestrictedSettingDialogIntent(
                            packageName, mName);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(LOG_TAG, "Cannot create enhanced confirmation restriction intent for"
                            + " package: " + packageName, e);
                }
            }
        }
        return getRestrictionIntentAsUser(user, context);
    }

    @Override
    public String toString() {
        return "Role{"
                + "mName='" + mName + '\''
                + ", mAllowBypassingQualification=" + mAllowBypassingQualification
                + ", mBehavior=" + mBehavior
                + ", mDefaultHoldersResourceName=" + mDefaultHoldersResourceName
                + ", mDescriptionResource=" + mDescriptionResource
                + ", mExclusivity=" + mExclusivity
                + ", mFallBackToDefaultHolder=" + mFallBackToDefaultHolder
                + ", mFeatureFlag=" + mFeatureFlag
                + ", mLabelResource=" + mLabelResource
                + ", mMaxSdkVersion=" + mMaxSdkVersion
                + ", mMinSdkVersion=" + mMinSdkVersion
                + ", mOnlyGrantWhenAdded=" + mOnlyGrantWhenAdded
                + ", mOverrideUserWhenGranting=" + mOverrideUserWhenGranting
                + ", mRequestDescriptionResource=" + mRequestDescriptionResource
                + ", mRequestTitleResource=" + mRequestTitleResource
                + ", mRequestable=" + mRequestable
                + ", mSearchKeywordsResource=" + mSearchKeywordsResource
                + ", mShortLabelResource=" + mShortLabelResource
                + ", mShowNone=" + mShowNone
                + ", mStatic=" + mStatic
                + ", mSystemOnly=" + mSystemOnly
                + ", mVisible=" + mVisible
                + ", mRequirements=" + mRequirements
                + ", mPermissions=" + mPermissions
                + ", mAppOpPermissions=" + mAppOpPermissions
                + ", mAppOps=" + mAppOps
                + ", mPreferredActivities=" + mPreferredActivities
                + ", mUiBehaviorName=" + mUiBehaviorName
                + '}';
    }
}
