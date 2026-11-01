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

package com.android.role.controller.behavior.v36;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.role.controller.model.Role;
import com.android.role.controller.model.RoleBehavior;
import com.android.role.controller.util.PackageUtils;
import com.android.role.controller.util.RoleFlags;
import com.android.role.controller.util.UserUtils;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public class ReservedForTestingProfileGroupExclusivityRoleBehavior implements RoleBehavior {
    @Nullable
    @Override
    public List<String> getDefaultHoldersAsUser(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Context context) {
        if (RoleFlags.isProfileGroupExclusivityAvailable()) {
            Context userContext = UserUtils.getUserContext(context, user);
            RoleManager userRoleManager = userContext.getSystemService(RoleManager.class);
            return userRoleManager.getDefaultHoldersForTest(role.getName());
        } else {
            return null;
        }
    }

    @Override
    public Boolean isVisibleAsUser(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Context context) {
        if (RoleFlags.isProfileGroupExclusivityAvailable()) {
            Context userContext = UserUtils.getUserContext(context, user);
            RoleManager userRoleManager = userContext.getSystemService(RoleManager.class);
            return userRoleManager.isRoleVisibleForTest(role.getName());
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public List<String> getQualifyingPackagesAsUser(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Context context) {
        if (RoleFlags.isProfileGroupExclusivityAvailable()) {
            Context userContext = UserUtils.getUserContext(context, user);
            RoleManager userRoleManager = userContext.getSystemService(RoleManager.class);
            List<String> qualifyingPackageNames =
                    userRoleManager.getDefaultHoldersForTest(role.getName());

            // When getQualifyingPackagesAsUser returns a package that isn't installed, Default App
            // Settings fails to load. Only return available packages.
            List<String> availableQualifyingPackageNames = new ArrayList<>();
            for (int i = 0; i < qualifyingPackageNames.size(); i++) {
                String qualifyingPackage = qualifyingPackageNames.get(i);
                ApplicationInfo applicationInfo =
                        PackageUtils.getApplicationInfoAsUser(qualifyingPackage, user, context);
                if (applicationInfo != null) {
                    availableQualifyingPackageNames.add(qualifyingPackage);
                }
            }
            return availableQualifyingPackageNames;
        } else {
            return null;
        }
    }
}
