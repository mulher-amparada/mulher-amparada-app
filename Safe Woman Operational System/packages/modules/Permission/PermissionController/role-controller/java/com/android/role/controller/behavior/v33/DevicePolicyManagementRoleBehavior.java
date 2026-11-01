/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.role.controller.behavior.v33;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Build;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.role.controller.model.Role;
import com.android.role.controller.model.RoleBehavior;
import com.android.role.controller.util.UserUtils;

/**
 * Class for behavior of the device policy management role.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
public class DevicePolicyManagementRoleBehavior implements RoleBehavior {

    @Override
    public Boolean shouldAllowBypassingQualification(@NonNull Role role,
            @NonNull Context context) {
        DevicePolicyManager devicePolicyManager =
                context.getSystemService(DevicePolicyManager.class);
        return devicePolicyManager.shouldAllowBypassingDevicePolicyManagementRoleQualification();
    }

    /**
     * Checks if the package having {@code packageName} is allowed to bypass the Device Policy
     * Management Role qualification checks.
     *
     * @return {@code true} if the package is allowed, {@code false} otherwise.
     */
    public boolean isPackageAllowedToBypassQualificationAsUser(@NonNull String packageName,
            @NonNull UserHandle user, @NonNull Context context) {
        if (android.app.admin.flags.Flags.secureAdbRoleBypassing()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            Context userContext = UserUtils.getUserContext(context, user);
            DevicePolicyManager userDevicePolicyManager =
                    userContext.getSystemService(DevicePolicyManager.class);
            return userDevicePolicyManager
                    .isPackageAllowedToBypassDevicePolicyManagementRoleQualification(packageName);
        }
        return true;
    }
}
