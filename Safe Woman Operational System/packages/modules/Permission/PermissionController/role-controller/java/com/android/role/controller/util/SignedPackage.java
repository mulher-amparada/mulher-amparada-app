/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.role.controller.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A package name with an optional signing certificate.
 */
public class SignedPackage {

    private static final String LOG_TAG = SignedPackage.class.getSimpleName();

    private static final String SIGNED_PACKAGE_SEPARATOR = ";";
    private static final String CERTIFICATE_SEPARATOR = ":";

    /**
     * The name of the package.
     */
    @NonNull
    private final String mPackageName;

    /**
     * The signing certificate of the package.
     */
    @Nullable
    private final byte[] mCertificate;

    public SignedPackage(@NonNull String packageName, @Nullable byte[] certificate) {
        mPackageName = packageName;
        mCertificate = certificate;
    }

    @NonNull
    public String getPackageName() {
        return mPackageName;
    }

    @Nullable
    public byte[] getCertificate() {
        return mCertificate;
    }

    /**
     * Parse a {@link SignedPackage} from a string input.
     *
     * @param input the package name, optionally followed by a colon and a signing certificate
     *              digest if it's not a system app
     *
     * @return the parsed {@link SignedPackage}, or {@code null} if the input is invalid
     */
    @Nullable
    public static SignedPackage parse(@NonNull String input) {
        String packageName;
        byte[] certificate;
        int certificateSeparatorIndex = input.indexOf(CERTIFICATE_SEPARATOR);
        if (certificateSeparatorIndex != -1) {
            packageName = input.substring(0, certificateSeparatorIndex);
            String certificateString = input.substring(certificateSeparatorIndex + 1);
            try {
                certificate = new Signature(certificateString).toByteArray();
            } catch (IllegalArgumentException e) {
                Log.w(LOG_TAG, "Cannot parse signing certificate: " + input, e);
                return null;
            }
        } else {
            packageName = input;
            certificate = null;
        }
        return new SignedPackage(packageName, certificate);
    }

    /**
     * Parse a list of {@link SignedPackage}s from a string input.
     *
     * @param input the package names, each optionally followed by a colon and a signing certificate
     *              digest if it's not a system app
     *
     * @return the parsed list of valid {@link SignedPackage}s
     */
    @NonNull
    public static List<SignedPackage> parseList(@NonNull String input) {
        if (TextUtils.isEmpty(input)) {
            return Collections.emptyList();
        }
        List<SignedPackage> signedPackages = new ArrayList<>();
        for (String signedPackageInput : input.split(SIGNED_PACKAGE_SEPARATOR)) {
            SignedPackage signedPackage = parse(signedPackageInput);
            if (signedPackage != null) {
                signedPackages.add(signedPackage);
            }
        }
        return signedPackages;
    }

    /**
     * Checks whether this signed package is available, i.e. it is installed, and either has the
     * specified signing certificate or is a system app if no signing certificate is specified.
     *
     * @param user the user of the package
     * @param context the {@code Context} to retrieve system services
     *
     * @return whether this signed package is available
     */
    public boolean isAvailableAsUser(@NonNull UserHandle user, @NonNull Context context) {
        if (mCertificate != null) {
            if (!PackageUtils.hasSigningCertificateAsUser(mPackageName, mCertificate, user,
                    context)) {
                Log.w(LOG_TAG, "Package doesn't have required signing certificate: "
                        + mPackageName);
                return false;
            }
        } else {
            ApplicationInfo applicationInfo =
                    PackageUtils.getApplicationInfoAsUser(mPackageName, user, context);
            if (applicationInfo == null) {
                Log.w(LOG_TAG, "Cannot get ApplicationInfo for package: " + mPackageName);
                return false;
            }
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                Log.w(LOG_TAG, "Package didn't specify a signing certificate and isn't a" +
                        " system app: " + mPackageName);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether an {@link ApplicationInfo} matches this signed package, i.e. it has the same
     * package name, and either has the specified signing certificate or is a system app if no
     * signing certificate is specified.
     *
     * @param applicationInfo the {@link ApplicationInfo} to check for
     * @param context the {@code Context} to retrieve system services
     *
     * @return whether the {@link ApplicationInfo} matches this signed package
     */
    public boolean matches(@NonNull ApplicationInfo applicationInfo, @NonNull Context context) {
        if (!Objects.equals(applicationInfo.packageName, mPackageName)) {
            return false;
        }
        if (mCertificate != null) {
            UserHandle user = UserHandle.getUserHandleForUid(applicationInfo.uid);
            if (!PackageUtils.hasSigningCertificateAsUser(mPackageName, mCertificate, user,
                    context)) {
                Log.w(LOG_TAG, "Package doesn't have required signing certificate: "
                        + mPackageName);
                return false;
            }
        } else {
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                Log.w(LOG_TAG, "Package didn't specify a signing certificate and isn't a" +
                        " system app: " + mPackageName);
                return false;
            }
        }
        return true;
    }
}
