/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.permissioncontroller.tests.mocking.role.model

import android.app.AppOpsManager
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Process
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.permissioncontroller.role.model.RoleParserInitializer
import com.android.role.controller.model.AppOp
import com.android.role.controller.model.Permission
import com.android.role.controller.model.PermissionSet
import com.android.role.controller.model.RequiredComponent
import com.android.role.controller.model.RequiredUsesPermission
import com.android.role.controller.model.Requirement
import com.android.role.controller.model.Role
import com.android.role.controller.model.RoleParser
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoleParserTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupBeforeClass() {
            RoleParserInitializer.initialize()
        }
    }

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val uiAutomation = instrumentation.uiAutomation
    private val targetContext = instrumentation.targetContext
    private val packageManager = targetContext.packageManager

    @Test
    fun parseRoles() {
        // We may need to call privileged APIs to determine availability of things.
        uiAutomation.adoptShellPermissionIdentity()
        try {
            RoleParser(targetContext, true).parse()
        } finally {
            uiAutomation.dropShellPermissionIdentity()
        }
    }

    @Test
    fun validateRoles() {
        // We may need to call privileged APIs to determine availability of things.
        uiAutomation.adoptShellPermissionIdentity()
        try {
            val xml = RoleParser(targetContext).parseRolesXml()
            requireNotNull(xml)
            validateRoles(xml.first, xml.second)
        } finally {
            uiAutomation.dropShellPermissionIdentity()
        }
    }

    fun validateRoles(permissionSets: Map<String, PermissionSet>, roles: Map<String, Role>) {
        for (permissionSet in permissionSets.values) {
            for (permission in permissionSet.permissions) {
                validatePermission(permission)
            }
        }

        for (role in roles.values) {
            if (!role.isAvailableByFeatureFlagAndSdkVersion) {
                continue
            }

            for (requirement in role.requirements) {
                validateRequirement(requirement)
            }

            for (permission in role.permissions) {
                validatePermission(permission)
            }

            for (appOp in role.appOps) {
                validateAppOp(appOp)
            }

            for (appOpPermission in role.appOpPermissions) {
                validateAppOpPermission(appOpPermission)
            }

            for (preferredActivity in role.preferredActivities) {
                require(preferredActivity.activity in role.requirements) {
                    "<activity> of <preferred-activity> not required in <requirements>," +
                        " role: ${role.name}, preferred activity: $preferredActivity"
                }
            }
        }
    }

    private fun validateRequirement(requirement: Requirement) {
        if (!requirement.isAvailable) {
            return
        }
        when (requirement) {
            is RequiredUsesPermission -> validatePermissionName(requirement.name)
            is RequiredComponent -> requirement.permission?.let { validatePermissionName(it) }
        }
    }

    private fun validatePermission(permission: Permission) {
        if (!permission.isAvailableAsUser(Process.myUserHandle(), targetContext)) {
            return
        }
        validatePermissionName(permission.name, true)
    }

    private fun validatePermissionName(
        permissionName: String,
        enforceIsRuntimeOrRole: Boolean = false,
    ) {
        val isAutomotive = packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
        // Skip validation for car permissions which may not be available on all build targets.
        if (!isAutomotive && permissionName.startsWith("android.car")) {
            return
        }

        val permissionInfo =
            try {
                packageManager.getPermissionInfo(permissionName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                throw IllegalArgumentException("Unknown permission: $permissionName", e)
            }

        if (enforceIsRuntimeOrRole) {
            require(
                permissionInfo.protection == PermissionInfo.PROTECTION_DANGEROUS ||
                    permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_ROLE ==
                        PermissionInfo.PROTECTION_FLAG_ROLE
            ) {
                "Permission is not a runtime or role permission: $permissionName"
            }
        }
    }

    private fun validateAppOpPermission(appOpPermission: Permission) {
        if (!appOpPermission.isAvailableAsUser(Process.myUserHandle(), targetContext)) {
            return
        }
        validateAppOpPermission(appOpPermission.name)
    }

    private fun validateAppOpPermission(permissionName: String) {
        val permissionInfo =
            try {
                packageManager.getPermissionInfo(permissionName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                throw IllegalArgumentException("Unknown app op permission: $permissionName", e)
            }
        require(
            permissionInfo.protectionFlags and PermissionInfo.PROTECTION_FLAG_APPOP ==
                PermissionInfo.PROTECTION_FLAG_APPOP
        ) {
            "Permission is not an app op permission: $permissionName"
        }
    }

    private fun validateAppOp(appOp: AppOp) {
        if (!appOp.isAvailableByFeatureFlagAndSdkVersion) {
            return
        }
        // This throws IllegalArgumentException if app op is unknown.
        val permissionName = AppOpsManager.opToPermission(appOp.name)
        if (permissionName != null) {
            val permissionInfo = packageManager.getPermissionInfo(permissionName, 0)
            require(permissionInfo.protection != PermissionInfo.PROTECTION_DANGEROUS) {
                "App op has an associated runtime permission: ${appOp.name}"
            }
        }
    }
}
