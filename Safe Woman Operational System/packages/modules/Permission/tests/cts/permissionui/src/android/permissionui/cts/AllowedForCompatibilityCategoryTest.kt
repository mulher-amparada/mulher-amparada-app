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
import android.os.Build
import android.permission.flags.Flags
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import android.text.Html
import android.util.Log
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.uiautomator.By
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.CINNAMON_BUN, codeName = "CinnamonBun")
@RequiresFlagsEnabled(Flags.FLAG_ACCESS_LOCAL_NETWORK_PERMISSION_ENABLED)
class AllowedForCompatibilityCategoryTest : BaseUsePermissionTest() {
    @get:Rule val checkFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()!!

    private val allowedCategory = getPermissionControllerResString(HEADER_ALLOWED_ID)!!
    private val allowedForCompatibilityCategory =
        getPermissionControllerResString(HEADER_ALLOWED_FOR_COMPATIBILITY_ID)!!
    private val notAllowedCategory = getPermissionControllerResString(HEADER_DENIED_ID)!!
    private val nearbyDevicesFooterText =
        Html.fromHtml(
                getPermissionControllerResString(
                    ALLOWED_FOR_COMPATIBILITY_NEARBY_DEVICES_FOOTER_ID
                ),
                0,
            )
            .toString()

    @Before
    fun setup() {
        assumeFalse(isTv)
        installPackage(TEST_APP_APK)
    }

    @After
    fun teardown() {
        uninstallPackage(TEST_APP_PACKAGE, false)
    }

    @Test
    fun testInitialCategorization_permissionApps_nearbyDevices() {
        startManagePermissionAppsActivity(Manifest.permission_group.NEARBY_DEVICES)
        verifyDisplayOrder(
            listOf(allowedForCompatibilityCategory, TEST_APP_NAME, notAllowedCategory)
        )
    }

    @Test
    fun testCategorizationByState_permissionApps_nearbyDevices() {
        startManagePermissionAppsActivity(Manifest.permission_group.NEARBY_DEVICES)
        clickPermissionControllerUi(TEST_APP_NAME, MAX_SEARCH_SWIPES)
        clicksDenyInSettings()
        clickDontAllowAnywayButton()
        startManagePermissionAppsActivity(Manifest.permission_group.NEARBY_DEVICES)
        verifyDisplayOrder(listOf(notAllowedCategory, TEST_APP_NAME))

        clickPermissionControllerUi(TEST_APP_NAME, MAX_SEARCH_SWIPES)
        clickAllowButton()
        startManagePermissionAppsActivity(Manifest.permission_group.NEARBY_DEVICES)
        verifyDisplayOrder(listOf(allowedCategory, TEST_APP_NAME, allowedForCompatibilityCategory))
    }

    @Test
    fun testNearbyDevicesCompatibilityFooterDisplay_permissionApps() {
        startManagePermissionAppsActivity(Manifest.permission_group.NEARBY_DEVICES)
        verifyDisplayOrder(listOf(notAllowedCategory, nearbyDevicesFooterText))
    }

    @Test
    fun testCategorizationByState_appPermissions_nearbyDevices() {
        val nearbyDevicesGroupText =
            getPermissionLabel(android.Manifest.permission.ACCESS_LOCAL_NETWORK)

        startManageAppPermissionsActivity(TEST_APP_PACKAGE)
        val initialItems = mutableListOf(allowedForCompatibilityCategory, nearbyDevicesGroupText)
        if (!isWatch) {
            // We don't display not allowed if it is empty.
            initialItems.add(notAllowedCategory)
        }
        initialItems.add(nearbyDevicesFooterText)
        verifyDisplayOrder(initialItems)

        clickTextWithFallback(nearbyDevicesGroupText)
        clicksDenyInSettings()
        clickDontAllowAnywayButton()
        startManageAppPermissionsActivity(TEST_APP_PACKAGE)
        verifyDisplayOrder(listOf(notAllowedCategory, nearbyDevicesGroupText))

        clickTextWithFallback(nearbyDevicesGroupText)
        clickAllowButton()
        startManageAppPermissionsActivity(TEST_APP_PACKAGE)
        val finalItems = mutableListOf(allowedCategory, nearbyDevicesGroupText)
        if (!isWatch) {
            finalItems.add(notAllowedCategory)
        }
        verifyDisplayOrder(finalItems)
    }

    // Fallback performs advanced scrolling which may be required in certain devices
    private fun clickTextWithFallback(groupText: String) {
        try {
            waitFindObject(By.text(groupText)).click()
        } catch (ignored: Throwable) {
            clickPermissionControllerUi(groupText, MAX_SEARCH_SWIPES)
        }
    }

    private fun clickAllowButton() {
        if (isAutomotive || isWatch) {
            clickPermissionControllerUi(
                By.text(getPermissionControllerString("app_permission_button_allow"))
            )
        } else {
            clickPermissionControllerUi(By.res(ALLOW_RADIO_BUTTON))
        }
    }

    private fun clickDontAllowAnywayButton() {
        if (isWatch) {
            // Watch uses icons.
            clickPermissionControllerUi(By.desc(getPermissionControllerString("ok")))
        } else {
            clickPermissionControllerUi(
                By.text(getPermissionControllerResString(DENY_ANYWAY_BUTTON_TEXT)!!)
            )
        }
    }

    private fun verifyDisplayOrder(items: List<String>) {
        if (isWatch) {
            verifyExists(items)
        } else {
            verifyDisplayOrderOfCollection(items)
        }
    }

    private fun verifyExists(items: List<String>) {
        // Wear hierarchy doesn't have collectionItemInfo.
        items.forEach { text ->
            val node = waitFindObject(By.text(text), SCROLL_TIMEOUT_MILLIS)
            assertThat(node).isNotNull()
        }
    }

    private fun verifyDisplayOrderOfCollection(items: List<String>) {
        val indexes =
            items.map { text ->
                var node = waitFindObject(By.text(text), SCROLL_TIMEOUT_MILLIS)
                while (node != null && node.accessibilityNodeInfo.collectionItemInfo == null) {
                    node = node.parent
                }
                assertThat(node).isNotNull()
                node.accessibilityNodeInfo.collectionItemInfo.rowIndex.also {
                    Log.d(TAG, "Found '$text' at rowIndex $it")
                }
            }
        assertWithMessage("Items $items should be in increasing order of row indices $indexes")
            .that(indexes)
            .isInStrictOrder()
    }

    companion object {
        private const val SCROLL_TIMEOUT_MILLIS = 120_000L

        private val TAG = AllowedForCompatibilityCategoryTest::class.java.simpleName

        private const val TEST_APP_PACKAGE = "android.permissionui.cts.ctsappthattequestsinternet36"
        private const val TEST_APP_APK =
            "/data/local/tmp/cts-permissionui/CtsAppThatRequestsInternet36.apk"
        private const val TEST_APP_NAME = "A-CtsAppThatRequestsInternet36"
        private const val MAX_SEARCH_SWIPES = 15
    }
}
