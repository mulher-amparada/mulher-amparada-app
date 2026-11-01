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
package android.permissionui.cts

import android.Manifest
import android.app.Instrumentation
import android.app.compat.CompatChanges
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Process
import android.os.SystemClock
import android.platform.test.rule.ScreenRecordRule
import android.provider.Settings
import android.util.Log
import androidx.test.filters.FlakyTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import com.android.compatibility.common.util.CddTest
import com.android.compatibility.common.util.DisableAnimationRule
import com.android.compatibility.common.util.SystemUtil.callWithShellPermissionIdentity
import com.android.compatibility.common.util.SystemUtil.eventually
import com.android.compatibility.common.util.SystemUtil.runShellCommand
import com.android.compatibility.common.util.SystemUtil.runShellCommandOrThrow
import com.android.compatibility.common.util.SystemUtil.runWithShellPermissionIdentity
import com.android.compatibility.common.util.UiAutomatorUtils2
import com.android.compatibility.common.util.UiAutomatorUtils2.assertWithUiDump
import com.android.sts.common.util.StsExtraBusinessLogicTestCase
import com.android.systemui.Flags.expandedPrivacyIndicatorsOnLargeScreen
import java.util.regex.Pattern
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val APK_PATH =
    "/data/local/tmp/cts-permissionui/CtsAppThatAccessesLocationPermission.apk"
private const val APP_LABEL = "CtsLocationAccess"
private const val APP_PKG = "android.permissionui.cts.appthataccesseslocation"
private const val SHELL_PKG = "com.android.shell"
private const val USE_LOCATION = "use_location"
private const val FINISH_EARLY = "finish_early"
private const val USE_INTENT_ACTION = "test.action.USE_LOCATION"
private const val PRIVACY_CHIP_ID = "com.android.systemui:id/privacy_chip"
private const val XR_PRIVACY_CHIP_BUTTON_ID = "com.android.systemui:id/privacy_indicator_button"
private const val PRIVACY_ITEM_ID = "com.android.systemui:id/privacy_item"
private const val INDICATORS_FLAG = "camera_mic_icons_enabled"
private const val WEAR_MIC_LABEL = "Microphone"
private const val LOCATION_INDICATORS_NOT_PRESENT = 430681066L
private const val IDLE_TIMEOUT_MILLIS: Long = 2000
private const val TIMEOUT_MILLIS: Long = 20000
private const val TV_MIC_INDICATOR_WINDOW_TITLE = "MicrophoneCaptureIndicator"

@ScreenRecordRule.ScreenRecord
@FlakyTest
class LocationIndicatorsPermissionTest : StsExtraBusinessLogicTestCase {
    private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.context
    private val uiDevice: UiDevice = UiDevice.getInstance(instrumentation)
    private val packageManager: PackageManager = context.packageManager

    private val isTv = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    private val isCar = packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
    private val isWatch = packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
    // This flag is set to true for tablet and desktop devices.
    private val isLargeScreen = expandedPrivacyIndicatorsOnLargeScreen()
    private val originalLocationLabel =
        packageManager
            .getPermissionGroupInfo(Manifest.permission_group.LOCATION, 0)
            .loadLabel(packageManager)
            .toString()
    private var isScreenOn = false
    private var screenTimeoutBeforeTest: Long = 0L
    private var locationEnabledBeforeTest: Boolean = false

    @get:Rule val disableAnimationRule = DisableAnimationRule()

    @get:Rule val screenRecordRule = ScreenRecordRule(false, false)

    constructor() : super()

    companion object {
        const val DELAY_MILLIS = 3000L
        private val TAG = LocationIndicatorsPermissionTest::class.java.simpleName
    }

    private fun uninstall() {
        val output = runShellCommand("pm uninstall $APP_PKG").trim()
        assertEquals("Success", output)
    }

    private fun install() {
        val output = runShellCommandOrThrow("pm install -g $APK_PATH").trim()
        assertEquals("Success", output)
    }

    @Before
    fun setUp() {
        runWithShellPermissionIdentity {
            screenTimeoutBeforeTest =
                Settings.System.getLong(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            Settings.System.putLong(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                1800000L,
            )
            locationEnabledBeforeTest =
                context.getSystemService(LocationManager::class.java)!!.isLocationEnabled
        }
        runShellCommandOrThrow("cmd location set-location-enabled true")

        if (!isScreenOn) {
            uiDevice.wakeUp()
            runShellCommand(instrumentation, "wm dismiss-keyguard")
            Thread.sleep(DELAY_MILLIS)
            isScreenOn = true
        }
        uiDevice.findObject(By.text("Close"))?.click()
        // If the change Id is not present, then isChangeEnabled will return true. To bypass this,
        // the change is set to "false" if present.
        assumeFalse(
            "feature not present on this device",
            callWithShellPermissionIdentity {
                CompatChanges.isChangeEnabled(LOCATION_INDICATORS_NOT_PRESENT, Process.SYSTEM_UID)
            },
        )
        install()
    }

    @After
    fun tearDown() {
        uninstall()
        runWithShellPermissionIdentity {
            Settings.System.putLong(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                screenTimeoutBeforeTest,
            )
        }
        runShellCommandOrThrow("cmd location set-location-enabled $locationEnabledBeforeTest")
        pressHome()
        pressHome()
    }

    private fun openApp(useLocation: Boolean) {
        context.startActivity(
            Intent(USE_INTENT_ACTION).apply {
                putExtra(USE_LOCATION, useLocation)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    @Test
    @CddTest(requirement = "9.8.8/C-0-6")
    fun testLocationIndicator() {
        testLocationIndicator(useLocation = true)
    }

    private fun testLocationIndicator(useLocation: Boolean) {
        Log.d(TAG, "testLocationIndicator useLocation=$useLocation")
        // Location indicators are not available or in scope for these form factors.
        assumeFalse(isWatch || isTv || isCar || isLargeScreen)
        openApp(useLocation)
        try {
            eventually {
                val appView = uiDevice.findObject(UiSelector().textContains(APP_LABEL))

                assertWithUiDump {
                    assertTrue("View with text $APP_LABEL not found", appView.exists())
                }
            }

            Log.d(TAG, "assert to make sure Indicators are displayed")
            assertIndicatorsShown(useLocation)
        } finally {}
    }

    private fun assertIndicatorsShown(useLocation: Boolean) {
        uiDevice.openQuickSettings()
        assertPrivacyChipAndIndicatorsPresent(useLocation)
        uiDevice.pressBack()
    }

    private fun assertPrivacyChipAndIndicatorsPresent(useLocation: Boolean) {
        // Ensure the privacy chip is present
        if (useLocation) {
            eventually {
                val privacyChip = UiAutomatorUtils2.waitFindObjectOrNull(By.res(PRIVACY_CHIP_ID))
                assertWithUiDump {
                    assertNotNull("view with id $PRIVACY_CHIP_ID not found", privacyChip)
                }
                privacyChip.click()
            }
        } else {
            Log.d(TAG, "waiting for PRIVACY_CHIP_ID to disappear")
            assertWithUiDump { UiAutomatorUtils2.waitUntilObjectGone(By.res(PRIVACY_CHIP_ID)) }
            return
        }

        eventually {
            if (useLocation) {
                val iconView =
                    waitFindObject(
                        By.desc(Pattern.compile(originalLocationLabel, Pattern.CASE_INSENSITIVE))
                    )
                assertWithUiDump {
                    assertNotNull(
                        "View with description '$originalLocationLabel' not found",
                        iconView,
                    )
                }
            }
            var appView = waitFindObject(By.textContains(APP_LABEL))
            assertWithUiDump { assertNotNull("View with text $APP_LABEL not found", appView) }
        }
        uiDevice.pressBack()
    }

    private fun pressBack() {
        uiDevice.pressBack()
    }

    private fun pressHome() {
        uiDevice.pressHome()
    }

    protected fun waitFindObject(selector: BySelector): UiObject2? {
        return findObjectWithRetry({ t -> UiAutomatorUtils2.waitFindObject(selector, t) })
    }

    private fun findObjectWithRetry(
        automatorMethod: (timeoutMillis: Long) -> UiObject2?,
        timeoutMillis: Long = TIMEOUT_MILLIS,
    ): UiObject2? {
        val startTime = SystemClock.elapsedRealtime()
        return try {
            automatorMethod(timeoutMillis)
        } catch (e: StaleObjectException) {
            val remainingTime = timeoutMillis - (SystemClock.elapsedRealtime() - startTime)
            if (remainingTime <= 0) {
                throw e
            }
            automatorMethod(remainingTime)
        }
    }
}
