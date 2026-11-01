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

package com.android.permissioncontroller.tests.mocking.permission.utils

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.permission.flags.Flags
import android.platform.test.annotations.AsbSecurityTest
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.modules.utils.build.SdkLevel
import com.android.permissioncontroller.permission.utils.v31.AdminRestrictedPermissionsUtils
import org.junit.Assert.assertEquals
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.mock

@RunWith(Enclosed::class)
object AdminRestrictedPermissionsUtilsTest {

    @get:Rule val checkFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dpm: DevicePolicyManager = mock(DevicePolicyManager::class.java)

    @RunWith(Parameterized::class)
    class AdminRestrictedPermissionsUtilsParameterizedTest(
        private val permission: String,
        private val group: String?,
        private val canAdminGrantSensorsPermissions: Boolean,
        private val expected: Boolean,
    ) {

        @Before
        fun setup() {
            Assume.assumeTrue(SdkLevel.isAtLeastS())
        }

        @AsbSecurityTest(cveBugId = [308138085])
        @Test
        fun mayAdminGrantPermissionTest() {
            val canGrant =
                AdminRestrictedPermissionsUtils.mayAdminGrantPermission(
                    permission,
                    group,
                    canAdminGrantSensorsPermissions,
                    false,
                    dpm,
                )
            assertEquals(expected, canGrant)
        }

        companion object {
            /**
             * Returns a list of arrays containing the following values:
             * 0. Permission name (String)
             * 1. Permission group name (String)
             * 2. Can admin grant sensors permissions (Boolean)
             * 3. Expected return from mayAdminGrantPermission method (Boolean)
             */
            @JvmStatic
            @Parameterized.Parameters(name = "{index}: validate({0}, {1}, {3}) = {4}")
            fun getParameters(): List<Array<out Any?>> {
                return listOf(
                    arrayOf("abc", "xyz", false, true),
                    arrayOf("abc", null, false, true),
                    arrayOf("android.permission.RECORD_AUDIO", "xyz", false, false),
                    arrayOf("abc", "android.permission-group.MICROPHONE", false, false),
                    arrayOf(
                        "android.permission.RECORD_AUDIO",
                        "android.permission-group.MICROPHONE",
                        false,
                        false,
                    ),
                    arrayOf(
                        "android.permission.RECORD_AUDIO",
                        "android.permission-group.MICROPHONE",
                        true,
                        true,
                    ),
                )
            }
        }
    }

    @RunWith(AndroidJUnit4::class)
    class AdminRestrictedPermissionsUtilsSingleTest {

        @Test
        @SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
        @RequiresFlagsEnabled(Flags.FLAG_REPLACE_BODY_SENSOR_PERMISSION_ENABLED)
        fun addAdminRestrictedPermission_addsPermissionToRestrictedList() {
            val exampleHealthPermission = "test.permission.health"
            var canGrant =
                AdminRestrictedPermissionsUtils.mayAdminGrantPermission(
                    exampleHealthPermission,
                    /* group= */ null,
                    /* canAdminGrantSensorsPermissions= */ false,
                    /* isManagedProfile= */ false,
                    dpm,
                )
            assertEquals(true, canGrant)

            AdminRestrictedPermissionsUtils.addAdminRestrictedPermission(exampleHealthPermission)

            canGrant =
                AdminRestrictedPermissionsUtils.mayAdminGrantPermission(
                    exampleHealthPermission,
                    /* group= */ null,
                    /* canAdminGrantSensorsPermissions= */ false,
                    /* isManagedProfile= */ false,
                    dpm,
                )
            assertEquals(false, canGrant)
        }
    }
}
