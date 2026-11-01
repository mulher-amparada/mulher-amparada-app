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
import android.app.permissionui.LocationButtonRequest
import android.app.permissionui.LocationButtonSession
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.permission.flags.Flags
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.CheckFlagsRule
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import android.view.SurfaceView
import androidx.test.filters.SdkSuppress
import androidx.test.uiautomator.By
import com.android.compatibility.common.util.CddTest
import com.android.compatibility.common.util.SystemUtil.eventually
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for the Location Button UI and functionality. */
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.CINNAMON_BUN, codeName = "CinnamonBun")
@RequiresFlagsEnabled(Flags.FLAG_LOCATION_BUTTON_ENABLED)
@RunWith(TestParameterInjector::class)
class LocationButtonTest : BaseUsePermissionTest() {

    @get:Rule val checkFlagsRule: CheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    @Before
    fun setup() {
        assumeFalse(isAutomotive)
        assumeFalse(isTv)
        assumeFalse(isWatch)
        installPackage(TEST_APP_APK_PATH, expectSuccess = true)
    }

    @After
    fun cleanup() {
        val skipUninstall = isAutomotive || isTv || isWatch
        uninstallPackage(TEST_APP_PACKAGE_NAME, requireSuccess = !skipUninstall)
    }

    @Test
    fun testLocationButton_grantsPreciseLocation() {
        assertAppHasPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            false,
            packageName = TEST_APP_PACKAGE_NAME,
        )
        val intent =
            Intent().apply {
                component =
                    ComponentName(
                        TEST_APP_PACKAGE_NAME,
                        "$TEST_APP_PACKAGE_NAME.LocationButtonActivity",
                    )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("width", dpToPx(200))
                putExtra("height", dpToPx(80))
            }
        context.startActivity(intent)

        val surfaceView = waitFindObject(By.clazz(SurfaceView::class.java.name))
        surfaceView.click()
        eventually { clickPermissionRequestAllowLocationButtonButton() }
        eventually {
            assertAppHasPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                true,
                packageName = TEST_APP_PACKAGE_NAME,
            )
        }
    }

    @Test
    fun testLocationButton_minSize_doesGrantPermission() {
        assertAppHasPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            false,
            packageName = TEST_APP_PACKAGE_NAME,
        )
        val intent =
            Intent().apply {
                component =
                    ComponentName(
                        TEST_APP_PACKAGE_NAME,
                        "$TEST_APP_PACKAGE_NAME.LocationButtonActivity",
                    )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("width", dpToPx(48))
                putExtra("height", dpToPx(48))
            }
        context.startActivity(intent)

        val surfaceView = waitFindObject(By.clazz(SurfaceView::class.java.name))
        surfaceView.click()
        eventually { clickPermissionRequestAllowLocationButtonButton() }
        eventually {
            assertAppHasPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                true,
                packageName = TEST_APP_PACKAGE_NAME,
            )
        }
    }

    @Test
    fun testLocationButton_buttonTooSmall_doesNotOpenPermissionDialog() {
        assertAppHasPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            false,
            packageName = TEST_APP_PACKAGE_NAME,
        )
        val intent =
            Intent().apply {
                component =
                    ComponentName(
                        TEST_APP_PACKAGE_NAME,
                        "$TEST_APP_PACKAGE_NAME.LocationButtonActivity",
                    )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("width", dpToPx(40))
                putExtra("height", dpToPx(40))
            }
        context.startActivity(intent)

        val surfaceView = waitFindObject(By.clazz(SurfaceView::class.java.name))
        surfaceView.click()
        findView(By.res(LOCATION_BUTTON_ALLOW_BUTTON), expected = false)
        assertAppHasPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            false,
            packageName = TEST_APP_PACKAGE_NAME,
        )
    }

    @Test
    fun testLocationButton_buttonTooLarge_doesNotOpenPermissionDialog() {
        assertAppHasPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            false,
            packageName = TEST_APP_PACKAGE_NAME,
        )
        val intent =
            Intent().apply {
                component =
                    ComponentName(
                        TEST_APP_PACKAGE_NAME,
                        "$TEST_APP_PACKAGE_NAME.LocationButtonActivity",
                    )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                // Too large width cause clipping, and location button won't work.
                val displayMetrics = context.resources.displayMetrics
                val maxScreenDimensionPx =
                    maxOf(displayMetrics.widthPixels, displayMetrics.heightPixels)
                val guaranteedClippingWidthPx = maxScreenDimensionPx + dpToPx(200)
                putExtra("width", guaranteedClippingWidthPx)
                putExtra("height", dpToPx(200)) // height is clamped down to max 136 dp.
            }
        context.startActivity(intent)

        val surfaceView = waitFindObject(By.clazz(SurfaceView::class.java.name))
        surfaceView.click()
        findView(By.res(LOCATION_BUTTON_ALLOW_BUTTON), expected = false)
        assertAppHasPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            false,
            packageName = TEST_APP_PACKAGE_NAME,
        )
    }

    @Test
    @CddTest(requirement = "3.8.18/C-0-1")
    fun testLocationButton_withTextType_rendersExpectedButtonLabel(
        @TestParameter textType: TextType
    ) {
        assertEquals(
            "CTS tests must be run in the en-US locale",
            "en-US",
            Locale.getDefault().toLanguageTag(),
        )
        val expectedButtonLabel = textType.expectedText

        val intent =
            Intent().apply {
                component =
                    ComponentName(
                        TEST_APP_PACKAGE_NAME,
                        "$TEST_APP_PACKAGE_NAME.LocationButtonActivity",
                    )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("width", dpToPx(300))
                putExtra("height", dpToPx(56))
                putExtra("text_type", textType.value)
            }
        context.startActivity(intent)

        waitFindObject(By.clazz(SurfaceView::class.java.name))
        waitFindObject(By.text(expectedButtonLabel))
    }

    @Test
    @CddTest(requirement = "3.8.18/C-0-1")
    fun testLocationButton_heightGreaterThanWidth_doesGrantPermission() {
        assertAppHasPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            false,
            packageName = TEST_APP_PACKAGE_NAME,
        )
        val intent =
            Intent().apply {
                component =
                    ComponentName(
                        TEST_APP_PACKAGE_NAME,
                        "$TEST_APP_PACKAGE_NAME.LocationButtonActivity",
                    )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("width", dpToPx(80))
                putExtra("height", dpToPx(130))
                putExtra("text_type", LocationButtonSession.TEXT_TYPE_NONE)
            }
        context.startActivity(intent)

        val surfaceView = waitFindObject(By.clazz(SurfaceView::class.java.name))
        uiDevice.waitForIdle()
        val surfaceBounds = surfaceView.visibleBounds
        assertTrue(
            "Button should be rendered taller than its width",
            surfaceBounds.height() > surfaceBounds.width(),
        )

        surfaceView.click()
        eventually { clickPermissionRequestAllowLocationButtonButton() }
        eventually {
            assertAppHasPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                true,
                packageName = TEST_APP_PACKAGE_NAME,
            )
        }
    }

    @Test
    fun testLocationButtonRequestBuilder() {
        val builder =
            LocationButtonRequest.Builder(200, 80, context.resources.configuration)
                .setPaddingLeft(10)
                .setPaddingTop(20)
                .setPaddingRight(30)
                .setPaddingBottom(40)
                .setBackgroundColor(0xFF112233.toInt())
                .setStrokeColor(0xFF445566.toInt())
                .setStrokeWidth(2)
                .setCornerRadius(5f)
                .setPressedCornerRadius(10f)
                .setIconTint(0xFF778899.toInt())
                .setTextType(LocationButtonSession.TEXT_TYPE_USE_PRECISE_LOCATION)
                .setTextColor(0xFFAABBCC.toInt())

        val request = builder.build()
        assertEquals(200, request.width)
        assertEquals(80, request.height)
        assertEquals(context.resources.configuration, request.configuration)
        assertEquals(10, request.paddingLeft)
        assertEquals(20, request.paddingTop)
        assertEquals(30, request.paddingRight)
        assertEquals(40, request.paddingBottom)
        assertEquals(0xFF112233.toInt(), request.backgroundColor)
        assertEquals(0xFF445566.toInt(), request.strokeColor)
        assertEquals(2, request.strokeWidth)
        assertEquals(5f, request.cornerRadius)
        assertEquals(10f, request.pressedCornerRadius)
        assertEquals(0xFF778899.toInt(), request.iconTint)
        assertEquals(LocationButtonSession.TEXT_TYPE_USE_PRECISE_LOCATION, request.textType)
        assertEquals(0xFFAABBCC.toInt(), request.textColor)
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    enum class TextType(val value: Int, val expectedText: String) {
        PRECISE_LOCATION(LocationButtonSession.TEXT_TYPE_PRECISE_LOCATION, "Precise location"),
        USE_PRECISE_LOCATION(
            LocationButtonSession.TEXT_TYPE_USE_PRECISE_LOCATION,
            "Use precise location",
        ),
        SHARE_PRECISE_LOCATION(
            LocationButtonSession.TEXT_TYPE_SHARE_PRECISE_LOCATION,
            "Share precise location",
        ),
        NEAR_MY_PRECISE_LOCATION(
            LocationButtonSession.TEXT_TYPE_NEAR_MY_PRECISE_LOCATION,
            "Near my precise location",
        ),
        NEAR_YOUR_PRECISE_LOCATION(
            LocationButtonSession.TEXT_TYPE_NEAR_YOUR_PRECISE_LOCATION,
            "Near your precise location",
        ),
    }

    companion object {
        private const val TEST_APP_PACKAGE_NAME = "android.permissionui.cts.locationbutton"
        private const val TEST_APP_APK_PATH = "$APK_DIRECTORY/CtsLocationButtonApp.apk"
    }
}
