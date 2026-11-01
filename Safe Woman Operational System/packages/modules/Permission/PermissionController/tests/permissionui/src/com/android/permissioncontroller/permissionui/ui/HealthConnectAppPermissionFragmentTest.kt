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

package com.android.permissioncontroller.permissionui.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.permission.flags.Flags.FLAG_REPLACE_BODY_SENSOR_PERMISSION_ENABLED
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import com.android.compatibility.common.util.SystemUtil.eventually
import com.android.compatibility.common.util.SystemUtil.runWithShellPermissionIdentity
import com.android.compatibility.common.util.UiAutomatorUtils2.waitFindObject
import com.android.compatibility.common.util.UiAutomatorUtils2.waitUntilObjectGone
import com.android.permissioncontroller.permissionui.wakeUpScreen
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple tests for {@link AppPermissionsFragment} for Health Connect behaviors Currently, does NOT
 * run on TV.
 *
 * TODO(b/178576541): Adapt and run on TV. Run with: atest HealthConnectAppPermissionFragmentTest
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, codeName = "UpsideDownCake")
class HealthConnectAppPermissionFragmentTest : BasePermissionUiTest() {

    @Rule @JvmField val mCheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    private lateinit var context: Context

    @Before fun assumeNotTelevision() = assumeFalse(isTelevision)

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context

        wakeUpScreen()
    }

    @After
    fun uninstallTestApp() {
        uninstallTestApps()
    }

    @SdkSuppress(
        minSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        maxSdkVersion = Build.VERSION_CODES.VANILLA_ICE_CREAM,
    )
    @Test
    fun usedHealthConnectPermissionsAreListed_handHeldDevices() {
        assumeFalse(context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        installTestAppThatUsesHealthConnectPermission()

        startManageAppPermissionsActivity()

        eventually { waitFindObject(By.text(HEALTH_CONNECT_LABEL)) }
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
    @Test
    fun usedHealthConnectPermissionsAreListed_handHeldDevices_healthFitnessBrand() {
        assumeFalse(context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        installTestAppThatUsesHealthConnectPermission()

        startManageAppPermissionsActivity()

        eventually { waitFindObject(By.text(HEALTH_FITNESS_LABEL)) }
    }

    @Test
    fun invalidUngrantedUsedHealthConnectPermissionsAreNotListed_handHeldDevices() {
        assumeFalse(context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        installInvalidTestAppThatUsesHealthConnectPermission()

        startManageAppPermissionsActivity()

        waitUntilObjectGone(By.text(HEALTH_CONNECT_LABEL), TIMEOUT_SHORT)
        waitUntilObjectGone(By.text(HEALTH_FITNESS_LABEL), TIMEOUT_SHORT)
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
    @RequiresFlagsEnabled(FLAG_REPLACE_BODY_SENSOR_PERMISSION_ENABLED)
    @Test
    fun startManageAppPermissionsActivity_wearDevices_requestLegacyBodySensorsUngranted_fitnessAndWellnessShowsUp() {
        assumeTrue(context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        installTestAppThatUsesLegacyBodySensorsPermissions()

        startManageAppPermissionsActivity()

        eventually { waitFindObject(By.text(FITNESS_AND_WELLNESS_LABEL)) }
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
    @RequiresFlagsEnabled(FLAG_REPLACE_BODY_SENSOR_PERMISSION_ENABLED)
    @Test
    fun startManageAppPermissionsActivity_wearDevices_requestReadHeartRateUngranted_fitnessAndWellnessShowsUp() {
        assumeTrue(context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        installTestAppThatUsesReadHeartRatePermissions()

        startManageAppPermissionsActivity()

        eventually { waitFindObject(By.text(FITNESS_AND_WELLNESS_LABEL)) }
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
    @RequiresFlagsEnabled(FLAG_REPLACE_BODY_SENSOR_PERMISSION_ENABLED)
    @Test
    fun startManageAppPermissionsActivity_handHeldDevices_requestLegacyBodySensorsUngranted_healthConnectShowsUp() {
        assumeFalse(context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        installTestAppThatUsesLegacyBodySensorsPermissions()

        startManageAppPermissionsActivity()

        eventually { waitFindObject(By.text(HEALTH_FITNESS_LABEL)) }
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
    @RequiresFlagsEnabled(FLAG_REPLACE_BODY_SENSOR_PERMISSION_ENABLED)
    @Test
    fun startManageAppPermissionsActivity_handHeldDevices_requestLegacyBodySensorsTargetSdk22Ungranted_healthConnectShowsUp() {
        assumeFalse(context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        installTestAppThatUsesLegacyBodySensorsPermissionsTargetSdk22()

        startManageAppPermissionsActivity()

        eventually { waitFindObject(By.text(HEALTH_FITNESS_LABEL)) }
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
    @RequiresFlagsEnabled(FLAG_REPLACE_BODY_SENSOR_PERMISSION_ENABLED)
    @Test
    fun startManageAppPermissionsActivity_handHeldDevices_requestReadHeartRateUngranted_healthConnectNotShowsUp() {
        assumeFalse(context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        installTestAppThatUsesReadHeartRatePermissions()

        startManageAppPermissionsActivity()

        waitUntilObjectGone(By.text(HEALTH_FITNESS_LABEL), TIMEOUT_SHORT)
    }

    private fun startManageAppPermissionsActivity() {
        runWithShellPermissionIdentity {
            instrumentationContext.startActivity(
                Intent(Intent.ACTION_MANAGE_APP_PERMISSIONS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(Intent.EXTRA_PACKAGE_NAME, PERM_USER_PACKAGE)
                }
            )
        }
    }

    companion object {
        private const val FITNESS_AND_WELLNESS_LABEL = "Fitness and wellness"
        // Health connect label uses a non breaking space
        private const val HEALTH_CONNECT_LABEL = "Health\u00A0Connect"
        private const val HEALTH_FITNESS_LABEL = "Health, fitness and wellness"
        private const val HEALTH_CONNECT_PERMISSION_READ_FLOORS_CLIMBED =
            "android.permission.health.READ_FLOORS_CLIMBED"

        private val TIMEOUT_SHORT = 500L
    }
}
