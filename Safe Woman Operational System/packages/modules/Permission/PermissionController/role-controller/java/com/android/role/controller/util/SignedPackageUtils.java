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
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods about {@link SignedPackage}.
 */
public final class SignedPackageUtils {

    private SignedPackageUtils() {}

    /**
     * Get a list of package names from a string input for {@link SignedPackage}s.
     * <p>
     * This method only returns packages that are
     * {@link SignedPackage#isAvailableAsUser(UserHandle, Context) available}.
     *
     * @param input the string input
     * @param user the user of the packages
     * @param context the {@code Context} to retrieve system services
     *
     * @return the package names
     */
    @NonNull
    public static List<String> getPackageNamesAsUser(@NonNull String input,
            @NonNull UserHandle user, @NonNull Context context) {
        List<SignedPackage> signedPackages = SignedPackage.parseList(input);
        List<String> packageNames = new ArrayList<>();
        int signedPackagesSize = signedPackages.size();
        for (int i = 0; i < signedPackagesSize; i++) {
            SignedPackage signedPackage = signedPackages.get(i);
            if (signedPackage.isAvailableAsUser(user, context)) {
                packageNames.add(signedPackage.getPackageName());
            }
        }
        return packageNames;
    }

    /**
     * Get a package name from a string input for a {@link SignedPackage}.
     * <p>
     * This method only returns a package if it is
     * {@link SignedPackage#isAvailableAsUser(UserHandle, Context) available}.
     *
     * @param input the string input
     * @param user the user of the package
     * @param context the {@code Context} to retrieve system services
     *
     * @return the package name, or {@code null} otherwise
     */
    @Nullable
    public static String getPackageNameAsUser(@NonNull String input, @NonNull UserHandle user,
            @NonNull Context context) {
        SignedPackage signedPackage = SignedPackage.parse(input);
        if (signedPackage == null || !signedPackage.isAvailableAsUser(user, context)) {
            return null;
        }
        return signedPackage.getPackageName();
    }

    /**
     * Check whether an {@link ApplicationInfo} matches any of the {@link SignedPackage}s.
     *
     * @param applicationInfo the {@link ApplicationInfo} to check for
     * @param signedPackages the list of {@link SignedPackage}s to check against
     * @param context the {@code Context} to retrieve system services
     *
     * @return whether the {@link ApplicationInfo} matches any of the {@link SignedPackage}s
     */
    public static boolean matchesAny(@NonNull ApplicationInfo applicationInfo,
            @NonNull List<SignedPackage> signedPackages, @NonNull Context context) {
        int signedPackagesSize = signedPackages.size();
        for (int i = 0; i < signedPackagesSize; i++) {
            SignedPackage signedPackage = signedPackages.get(i);
            if (signedPackage.matches(applicationInfo, context)) {
                return true;
            }
        }
        return false;
    }
}
