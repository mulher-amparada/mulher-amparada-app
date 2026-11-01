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

package com.android.role.controller.behavior.v37;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import com.android.role.controller.model.Permissions;
import com.android.role.controller.model.Role;
import com.android.role.controller.model.RoleBehavior;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for behavior of the "Watch Face" role. This role is only available on watch form-factor.
 */
public class WatchFaceRoleBehavior implements RoleBehavior {
    @Override
    public boolean isAvailableAsUser(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH);
    }

    @Override
    public void grantAsUser(@NonNull Role role, @NonNull String packageName, boolean overrideUser,
            @NonNull UserHandle user, @NonNull Context context) {
        Permissions.grantAsUser(packageName, getNonAndroidPermissions(context),
            /* ignoreDisabledSystemPackage= */ true, /* overrideUserSetAndFixed= */ false,
            /* setGrantedByRole= */ true, /* setGrantedByDefault= */ false,
            /* setSystemFixed= */ false, user, context);
    }

    @Override
    public void revokeAsUser(@NonNull Role role, @NonNull String packageName,
            @NonNull UserHandle user, @NonNull Context context) {
        Permissions.revokeAsUser(packageName, getNonAndroidPermissions(context),
            /* onlyIfGrantedByRole= */ true, /* onlyIfGrantedByDefault= */ false,
            /* overrideSystemFixed= */ false, user, context);
    }

    private @NonNull List<String> getNonAndroidPermissions(@NonNull Context context) {
        String[] configPermissions = context.getResources().getStringArray(
               android.R.array.config_watchFacePermissions);
        List<String> filteredPermissions = new ArrayList<>();
        for (String permission : configPermissions) {
            // Only consider non-android permissions
            if (!(permission.startsWith("android.") || permission.startsWith("com.android."))) {
                filteredPermissions.add(permission);
            }
        }
        return filteredPermissions;
    }
}
