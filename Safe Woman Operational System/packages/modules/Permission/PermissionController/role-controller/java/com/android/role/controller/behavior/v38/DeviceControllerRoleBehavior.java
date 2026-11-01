/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.role.controller.behavior.v38;

import android.app.admin.DevicePolicyManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.role.controller.model.RoleBehavior;
import com.android.role.controller.model.Role;
import com.android.role.controller.util.UserUtils;

import java.util.List;

/**
 * Class for behavior of the DEVICE_CONTROLLER role. This role is only available on enrolled
 * devices.
 */
public class DeviceControllerRoleBehavior implements RoleBehavior {

    private static final String LOG_TAG = DeviceControllerRoleBehavior.class.getSimpleName();

    @Override
    public boolean isAvailableAsUser(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Context context) {
        return UserManager.isHeadlessSystemUserMode() && user.isSystem();
    }

    @Override
    public Boolean isPackageQualifiedAsUser(@NonNull Role role, @NonNull String packageName,
            @NonNull UserHandle user, @NonNull Context context) {
        Context userContext = UserUtils.getUserContext(context, user);
        DevicePolicyManager userDevicePolicyManager =
            userContext.getSystemService(DevicePolicyManager.class);
        RoleManager userRoleManager = userContext.getSystemService(RoleManager.class);
        final List<String> deviceControllerRoleHolders =
            userRoleManager.getRoleHolders(RoleManager.ROLE_DEVICE_CONTROLLER);
        if (deviceControllerRoleHolders.contains(packageName)) {
            return true;
        }
        final List<String> devicePolicyManagementRoleHolders =
            userRoleManager.getRoleHolders(RoleManager.ROLE_DEVICE_POLICY_MANAGEMENT);
        if (!devicePolicyManagementRoleHolders.contains(packageName)) {
            Log.w(LOG_TAG,
                "Package " + packageName + " does not hold ROLE_DEVICE_POLICY_MANAGEMENT");
            return false;
        }
        if (userDevicePolicyManager.checkProvisioningPrecondition(
                DevicePolicyManager.ACTION_PROVISION_MULTIUSER_MANAGED_DEVICE, packageName)
                != DevicePolicyManager.STATUS_OK) {
            Log.w(LOG_TAG,
                "DevicePolicyManager.checkProvisioningPrecondition() returned false for package: "
                + packageName);
            return false;
        }
        return true;
    }
}
