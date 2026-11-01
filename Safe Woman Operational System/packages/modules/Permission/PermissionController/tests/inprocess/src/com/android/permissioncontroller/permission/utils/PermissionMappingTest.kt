/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.permissioncontroller.permission.utils

import android.Manifest
import android.app.AppOpsManager
import android.health.connect.HealthPermissions
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionMappingTest {

    @JvmField @Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testGetPlatformPermissionGroupForOp_healthPermissionGroup() {
        assertThat(
                PermissionMapping.getPlatformPermissionGroupForOp(
                    AppOpsManager.OPSTR_READ_WRITE_HEALTH_DATA
                )
            )
            .isEqualTo(HealthPermissions.HEALTH_PERMISSION_GROUP)
    }

    @Test
    fun testGetPlatformPermissionGroupForOp_microphone() {
        assertThat(
                PermissionMapping.getPlatformPermissionGroupForOp(
                    AppOpsManager.OPSTR_PHONE_CALL_MICROPHONE
                )
            )
            .isEqualTo(Manifest.permission_group.MICROPHONE)
    }

    @Test
    fun testGetPlatformPermissionGroupForOp_camera() {
        assertThat(
                PermissionMapping.getPlatformPermissionGroupForOp(
                    AppOpsManager.OPSTR_PHONE_CALL_CAMERA
                )
            )
            .isEqualTo(Manifest.permission_group.CAMERA)
    }

    @Test
    fun testGetPlatformPermissionGroupForOp_InvalidOpName() {
        try {
            assertThat(PermissionMapping.getPlatformPermissionGroupForOp("invalid_opName"))
                .isEqualTo(null)
        } catch (e: IllegalArgumentException) {
            // ignore wtf may throw in some configuration.
        }
    }

    @Test
    fun testGetPlatformPermissionGroupForOp_readContacts() {
        assertThat(
                PermissionMapping.getPlatformPermissionGroupForOp(AppOpsManager.OPSTR_READ_CONTACTS)
            )
            .isEqualTo(
                PermissionMapping.getGroupOfPlatformPermission(Manifest.permission.READ_CONTACTS)
            )
    }

    @Test
    fun testHealthPermissionIsRuntime_healthPermissionUiEnabled_isRuntime() {
        assumeTrue(Utils.isHealthPermissionUiEnabled())

        assertThat(PermissionMapping.isRuntimePlatformPermission(HealthPermissions.READ_HEART_RATE))
            .isTrue()
    }

    @Test
    fun testHealthPermissionGroupIsPlatform_healthPermissionUiEnabled_isPlatform() {
        assumeTrue(Utils.isHealthPermissionUiEnabled())

        assertThat(
                PermissionMapping.isPlatformPermissionGroup(
                    HealthPermissions.HEALTH_PERMISSION_GROUP
                )
            )
            .isTrue()
    }

    @Test
    fun testGetGroupForHealthPermission_healthPermissionUiEnabled_isHealthPermissionGroup() {
        assumeTrue(Utils.isHealthPermissionUiEnabled())

        assertThat(
                PermissionMapping.getGroupOfPlatformPermission(HealthPermissions.READ_HEART_RATE)
            )
            .isEqualTo(HealthPermissions.HEALTH_PERMISSION_GROUP)
    }

    @Test
    fun testGetPermNameForHealthPermissionGroup_healthPermissionUiEnabled_isHealthPermission() {
        assumeTrue(Utils.isHealthPermissionUiEnabled())

        assertThat(
                PermissionMapping.getPlatformPermissionNamesOfGroup(
                    HealthPermissions.HEALTH_PERMISSION_GROUP
                )
            )
            .contains(HealthPermissions.READ_HEART_RATE)
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
    @Test
    fun getGroupOfPlatformPermission_postBaklava_notHaveSensorsGroup() {
        assertNull(PermissionMapping.getGroupOfPlatformPermission(Manifest.permission.BODY_SENSORS))
        assertNull(
            PermissionMapping.getGroupOfPlatformPermission(
                Manifest.permission.BODY_SENSORS_BACKGROUND
            )
        )
    }

    @SdkSuppress(
        minSdkVersion = Build.VERSION_CODES.TIRAMISU,
        maxSdkVersion = Build.VERSION_CODES.VANILLA_ICE_CREAM,
    )
    @Test
    fun getGroupOfPlatformPermission_preBaklava_haveSensorsGroup() {
        assertNotNull(
            PermissionMapping.getGroupOfPlatformPermission(Manifest.permission.BODY_SENSORS)
        )
        assertNotNull(
            PermissionMapping.getGroupOfPlatformPermission(
                Manifest.permission.BODY_SENSORS_BACKGROUND
            )
        )
    }
}
