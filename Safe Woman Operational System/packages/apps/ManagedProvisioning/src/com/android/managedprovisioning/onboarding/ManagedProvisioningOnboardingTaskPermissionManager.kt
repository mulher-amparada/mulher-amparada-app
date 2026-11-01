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

package com.android.managedprovisioning.onboarding

import android.Manifest.permission
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.android.managedprovisioning.common.ProvisionLogger
import com.android.onboarding.tasks.security.TaskPermissionManager

/** Manages task permissions for Managed Provisioning onboarding task. */
object ManagedProvisioningOnboardingTaskPermissionManager : TaskPermissionManager() {

    /**
     * Checks if the caller is authorized to run onboarding tasks.
     *
     * A caller is authorized if it is the device policy management role holder or
     * setup wizard.
     *
     * @param context The application context.
     * @param callerPackageNames A set of package names associated with the calling UID.
     * @return true if the caller is authorized, false otherwise.
     */
    @RequiresPermission(anyOf = [permission.MANAGE_ROLE_HOLDERS, permission.GET_ROLE_HOLDERS])
    override fun isCallerAuthorized(context: Context, callerPackageNames: Set<String>): Boolean {
        if (isSetupWizard(context, callerPackageNames)) {
            return true
        }
        val devicePolicyManager =
            context.getSystemService(DevicePolicyManager::class.java)
        if (devicePolicyManager == null) {
            ProvisionLogger.loge("Failed to get DevicePolicyManager")
            return false
        }
        return devicePolicyManager.devicePolicyManagementRoleHolderPackage in callerPackageNames
    }

    private fun isSetupWizard(context: Context, callerPackageNames: Set<String>): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_SETUP_WIZARD)
        val resolveInfos = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_DIRECT_BOOT_AWARE
                    or PackageManager.MATCH_DIRECT_BOOT_UNAWARE
        )
        return resolveInfos.any { it.activityInfo.packageName in callerPackageNames }
    }
}
