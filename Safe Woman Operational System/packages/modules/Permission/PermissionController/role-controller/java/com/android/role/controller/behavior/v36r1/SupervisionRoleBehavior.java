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

package com.android.role.controller.behavior.v36r1;

import android.app.supervision.SupervisionManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.UserHandle;
import android.permission.flags.Flags;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.role.controller.model.Role;
import com.android.role.controller.model.RoleBehavior;
import com.android.role.controller.util.PackageUtils;
import com.android.role.controller.util.SignedPackage;
import com.android.role.controller.util.SignedPackageUtils;

import java.util.List;
import java.util.Objects;

/**
 * Class for behavior of the supervision role.
 *
 * <p>For a package to qualify as the role holder, it must be specified as an allowed package in
 * config_allowedSupervisionRolePackages or the config_systemSupervision package.
 */
public class SupervisionRoleBehavior implements RoleBehavior {

    private static final String LOG_TAG = SupervisionRoleBehavior.class.getSimpleName();

    /*
     * This config is temporary and intentionally not exposed as a system API and must be
     * accessed by name.
     */
    private static final String CONFIG_ALLOWED_SUPERVISION_ROLE_PACKAGES =
            "config_allowedSupervisionRolePackages";

    @Nullable
    @Override
    public Boolean isPackageQualifiedAsUser(@NonNull Role role, @NonNull String packageName,
            @NonNull UserHandle user, @NonNull Context context) {
        if (!Flags.supervisionRoleEnabled()) {
            return false;
        }

        return isSystemSupervisionPackage(packageName, context)
                || isAllowedPackage(packageName, user, context);
    }

    @Override
    @Nullable
    public Boolean shouldAllowBypassingQualification(@NonNull Role role, @NonNull Context context) {
        if (!Flags.supervisionRoleEnabled()) {
            return false;
        }

        SupervisionManager supervisionManager = context.getSystemService(SupervisionManager.class);
        return supervisionManager.shouldAllowBypassingSupervisionRoleQualification();
    }

    private boolean isSystemSupervisionPackage(@NonNull String packageName,
            @NonNull Context context) {
        return Objects.equals(context.getString(android.R.string.config_systemSupervision),
                packageName);
    }

    private boolean isAllowedPackage(
            @NonNull String packageName, @NonNull UserHandle user, @NonNull Context context) {
        ApplicationInfo applicationInfo =
                PackageUtils.getApplicationInfoAsUser(packageName, user, context);
        if (applicationInfo == null) {
            return false;
        }

        int resourceId = context.getResources().getIdentifier(
                CONFIG_ALLOWED_SUPERVISION_ROLE_PACKAGES, "string", "android");
        if (resourceId == 0) {
            Log.w(LOG_TAG, "Cannot find resource for: " + CONFIG_ALLOWED_SUPERVISION_ROLE_PACKAGES);
            return false;
        }

        String config;
        try {
            config = context.getString(resourceId);
        } catch (Resources.NotFoundException e) {
            Log.w(LOG_TAG, "Cannot get resource: " + CONFIG_ALLOWED_SUPERVISION_ROLE_PACKAGES, e);
            return false;
        }

        List<SignedPackage> signedPackages = SignedPackage.parseList(config);
        return SignedPackageUtils.matchesAny(applicationInfo, signedPackages, context);
    }
}
