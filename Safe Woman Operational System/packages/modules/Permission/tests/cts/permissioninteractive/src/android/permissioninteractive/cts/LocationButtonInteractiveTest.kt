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

package android.permissioninteractive.cts

import android.app.permissionui.LocationButtonSession
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.permission.flags.Flags
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.CheckFlagsRule
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.android.compatibility.common.util.CddTest
import com.android.compatibility.common.util.SystemUtil.runShellCommand
import com.android.interactive.Step
import com.android.interactive.annotations.Interactive
import com.android.interactive.annotations.NotFullyAutomated
import com.android.interactive.steps.locationbutton.VerifyButtonShapeMorphs
import com.android.interactive.steps.locationbutton.VerifyColorsStep
import com.android.interactive.steps.locationbutton.VerifyIconOnlyStep
import com.android.interactive.steps.locationbutton.VerifyPaddingsStep
import com.android.interactive.steps.locationbutton.VerifyRtlLanguageStep
import com.android.interactive.steps.locationbutton.VerifySharpCornersStep
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Interactive CTS tests for the Location Button. */
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.CINNAMON_BUN, codeName = "CinnamonBun")
@RequiresFlagsEnabled(Flags.FLAG_LOCATION_BUTTON_ENABLED)
@RunWith(AndroidJUnit4::class)
class LocationButtonInteractiveTest {

    @get:Rule val checkFlagsRule: CheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    @Before
    fun setup() {
        assumeFalse(isAutomotive)
        assumeFalse(isTv)
        assumeFalse(isWatch)
    }

    @After
    fun tearDown() {
        runShellCommand("am force-stop $TEST_APP_PACKAGE_NAME")
    }

    @Interactive
    @Test
    @CddTest(requirement = "3.8.18/C-0-1")
    @NotFullyAutomated(
        reason =
            "Visual properties of the Location Button are rendered remotely by SystemUI " +
                "and cannot be reliably verified via automation."
    )
    fun verifyButtonColorCustomization() {
        launchLocationButtonApp(
            backgroundColor = Color.BLUE,
            textColor = Color.WHITE,
            iconTint = Color.WHITE,
            strokeColor = Color.BLACK,
            strokeWidth = dpToPx(3),
        )
        assertTrue(Step.execute(VerifyColorsStep::class.java))
    }

    @Interactive
    @Test
    @CddTest(requirement = "3.8.18/C-0-1")
    @NotFullyAutomated(
        reason =
            "Visual properties of the Location Button are rendered remotely by SystemUI " +
                "and cannot be reliably verified via automation."
    )
    fun verifyButtonShapeCustomization() {
        launchLocationButtonApp(cornerRadius = 0f, pressedCornerRadius = 0f)
        assertTrue(Step.execute(VerifySharpCornersStep::class.java))
    }

    @Interactive
    @Test
    @CddTest(requirement = "3.8.18/C-0-1")
    @NotFullyAutomated(
        reason =
            "Visual properties of the Location Button are rendered remotely by SystemUI " +
                "and cannot be reliably verified via automation."
    )
    fun verifyButtonShapeMorphsOnPress() {
        launchLocationButtonApp(cornerRadius = dpToPx(30).toFloat(), pressedCornerRadius = 0f)
        assertTrue(Step.execute(VerifyButtonShapeMorphs::class.java))
    }

    @Interactive
    @Test
    @CddTest(requirement = "3.8.18/C-0-1")
    @NotFullyAutomated(
        reason =
            "Visual properties of the Location Button are rendered remotely by SystemUI " +
                "and cannot be reliably verified via automation."
    )
    fun verifyButtonIconOnly() {
        launchLocationButtonApp(textType = LocationButtonSession.TEXT_TYPE_NONE)
        assertTrue(Step.execute(VerifyIconOnlyStep::class.java))
    }

    @Interactive
    @Test
    @CddTest(requirement = "3.8.18/C-0-1")
    @NotFullyAutomated(
        reason =
            "Visual properties of the Location Button are rendered remotely by SystemUI " +
                "and cannot be reliably verified via automation."
    )
    fun verifyButtonSupportsRtlLayout() {
        launchLocationButtonApp(languageTag = "ar")
        assertTrue(Step.execute(VerifyRtlLanguageStep::class.java))
    }

    @Interactive
    @Test
    @CddTest(requirement = "3.8.18/C-0-1")
    @NotFullyAutomated(
        reason =
            "Visual properties of the Location Button are rendered remotely by SystemUI " +
                "and cannot be reliably verified via automation."
    )
    fun verifyButtonPaddingCustomization() {
        launchLocationButtonApp(
            width = dpToPx(48),
            height = dpToPx(48),
            textType = LocationButtonSession.TEXT_TYPE_NONE,
            paddingLeft = dpToPx(8),
            paddingTop = dpToPx(8),
            paddingRight = dpToPx(8),
            paddingBottom = dpToPx(8),
            showReferenceButton = true,
        )
        assertTrue(Step.execute(VerifyPaddingsStep::class.java))
    }

    private fun launchLocationButtonApp(
        width: Int = dpToPx(250),
        height: Int = dpToPx(60),
        textType: Int = LocationButtonSession.TEXT_TYPE_USE_PRECISE_LOCATION,
        backgroundColor: Int = Color.BLUE,
        textColor: Int = Color.WHITE,
        iconTint: Int = Color.WHITE,
        strokeColor: Int = Color.BLACK,
        strokeWidth: Int = dpToPx(3),
        cornerRadius: Float = dpToPx(28).toFloat(),
        pressedCornerRadius: Float = dpToPx(28).toFloat(),
        languageTag: String? = null,
        paddingLeft: Int = 0,
        paddingTop: Int = 0,
        paddingRight: Int = 0,
        paddingBottom: Int = 0,
        showReferenceButton: Boolean = false,
    ) {
        val intent =
            Intent().apply {
                component =
                    ComponentName(
                        TEST_APP_PACKAGE_NAME,
                        "$TEST_APP_PACKAGE_NAME.LocationButtonActivity",
                    )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(EXTRA_WIDTH, width)
                putExtra(EXTRA_HEIGHT, height)
                putExtra(EXTRA_TEXT_TYPE, textType)
                putExtra(EXTRA_BACKGROUND_COLOR, backgroundColor)
                putExtra(EXTRA_TEXT_COLOR, textColor)
                putExtra(EXTRA_ICON_TINT, iconTint)
                putExtra(EXTRA_STROKE_COLOR, strokeColor)
                putExtra(EXTRA_STROKE_WIDTH, strokeWidth)
                putExtra(EXTRA_CORNER_RADIUS, cornerRadius)
                putExtra(EXTRA_PRESSED_CORNER_RADIUS, pressedCornerRadius)
                languageTag?.let { putExtra(EXTRA_LANGUAGE_TAG, it) }
                putExtra(EXTRA_PADDING_LEFT, paddingLeft)
                putExtra(EXTRA_PADDING_TOP, paddingTop)
                putExtra(EXTRA_PADDING_RIGHT, paddingRight)
                putExtra(EXTRA_PADDING_BOTTOM, paddingBottom)
                putExtra(EXTRA_SHOW_REFERENCE_BUTTON, showReferenceButton)
            }
        context.startActivity(intent)
        assertTrue(
            "LocationButton unexpectedly not shown",
            uiDevice.wait(Until.hasObject(By.desc(LOCATION_BUTTON_CONTENT_DESCRIPTION)), 5000),
        )
        uiDevice.waitForIdle()
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    companion object {
        private val instrumentation = InstrumentationRegistry.getInstrumentation()
        private val context = instrumentation.targetContext
        private val packageManager = context.packageManager
        private val uiDevice = UiDevice.getInstance(instrumentation)
        private val isTv = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        private val isWatch = packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
        private val isAutomotive =
            packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

        private const val TEST_APP_PACKAGE_NAME = "android.permissioninteractive.cts.locationbutton"
        private const val EXTRA_WIDTH = "width"
        private const val EXTRA_HEIGHT = "height"
        private const val EXTRA_BACKGROUND_COLOR = "background_color"
        private const val EXTRA_TEXT_COLOR = "text_color"
        private const val EXTRA_ICON_TINT = "icon_tint"
        private const val EXTRA_STROKE_COLOR = "stroke_color"
        private const val EXTRA_STROKE_WIDTH = "stroke_width"
        private const val EXTRA_CORNER_RADIUS = "corner_radius"
        private const val EXTRA_PRESSED_CORNER_RADIUS = "pressed_corner_radius"
        private const val EXTRA_TEXT_TYPE = "text_type"
        private const val EXTRA_LANGUAGE_TAG = "language_tag"
        private const val EXTRA_PADDING_LEFT = "padding_left"
        private const val EXTRA_PADDING_TOP = "padding_top"
        private const val EXTRA_PADDING_RIGHT = "padding_right"
        private const val EXTRA_PADDING_BOTTOM = "padding_bottom"
        private const val EXTRA_SHOW_REFERENCE_BUTTON = "show_reference_button"
        private const val LOCATION_BUTTON_CONTENT_DESCRIPTION = "location_button"
    }
}
