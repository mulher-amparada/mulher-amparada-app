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
package android.app.role.cts

import android.app.Activity
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.permission.flags.Flags.FLAG_ASSIST_SETTINGS_PRIVACY_IMPROVEMENTS_ENABLED
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import android.util.Pair
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.android.compatibility.common.util.DisableAnimationRule
import com.android.compatibility.common.util.FreezeRotationRule
import com.android.compatibility.common.util.SettingsStateKeeperRule
import com.android.compatibility.common.util.SettingsStateManager
import com.android.compatibility.common.util.SystemUtil
import com.android.compatibility.common.util.UiAutomatorUtils2
import com.google.common.truth.Truth
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests {@link RequestReadScreenContextActivity} */
// TODO: update to correct version once available
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
@RunWith(AndroidJUnit4::class)
@RequiresFlagsEnabled(FLAG_ASSIST_SETTINGS_PRIVACY_IMPROVEMENTS_ENABLED)
class RequestReadScreenContextTest {
    @get:Rule val checkFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()
    @get:Rule val disableAnimationRule = DisableAnimationRule()
    @get:Rule val freezeRotationRule = FreezeRotationRule()
    @get:Rule val activityRule = ActivityTestRule(WaitForResultActivity::class.java)
    @get:Rule
    val readScreenContextDeniedCountManagersKeeper =
        SettingsStateKeeperRule(context, READ_SCREEN_CONTEXT_REQUEST_DENIED_COUNT)

    private val readScreenContextDeniedCountManager =
        SettingsStateManager(context, READ_SCREEN_CONTEXT_REQUEST_DENIED_COUNT)

    private val appOpsManager = context.getSystemService(AppOpsManager::class.java)!!
    private val roleManager = context.getSystemService(RoleManager::class.java)!!

    private var assistantRoleHolderPackageUid: Int = Process.INVALID_UID

    private var roleHolder: String? = null

    @Before
    fun setUp() {
        val packageManager = context.packageManager
        assumeFalse(packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE))
        assumeFalse(packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK))
        assumeFalse(packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH))
        assumeTrue(RoleManagerUtil.isCddCompliantScreenSize())
        saveRoleHolder()
        installPackage(APP_APK_PATH)
        assistantRoleHolderPackageUid = context.packageManager.getPackageUid(APP_PACKAGE_NAME, 0)

        setReadScreenContextRequestDeniedCount(0)
    }

    @After
    fun tearDown() {
        // Close dialog, if a test failed it might be left open
        pressBack()

        uninstallPackage(APP_PACKAGE_NAME)
        restoreRoleHolder()
    }

    @Test
    fun startRequestReadScreenContextActivity_finishIfNotRoleHolder() {
        setAppOpMode(AppOpsManager.MODE_IGNORED)
        requestReadScreenContext()
        val result = waitForResult()
        Truth.assertThat(result.first).isEqualTo(Activity.RESULT_CANCELED)
        Truth.assertThat(getAppOpMode()).isEqualTo(AppOpsManager.MODE_IGNORED)
    }

    @Test
    fun startRequestReadScreenContextActivity_finishIfAlreadyAllowed() {
        addRoleHolder(RoleManager.ROLE_ASSISTANT, APP_PACKAGE_NAME)
        setAppOpMode(AppOpsManager.MODE_ALLOWED)

        requestReadScreenContext()
        val result = waitForResult()
        Truth.assertThat(result.first).isEqualTo(Activity.RESULT_OK)
        Truth.assertThat(getAppOpMode()).isEqualTo(AppOpsManager.MODE_ALLOWED)
    }

    @Test
    fun startRequestReadScreenContextActivity_finishIfUnrequestable() {
        addRoleHolder(RoleManager.ROLE_ASSISTANT, APP_PACKAGE_NAME)
        setAppOpMode(AppOpsManager.MODE_IGNORED)
        setReadScreenContextRequestDeniedCount(100)

        requestReadScreenContext()
        val result = waitForResult()
        Truth.assertThat(result.first).isEqualTo(Activity.RESULT_CANCELED)
        Truth.assertThat(getAppOpMode()).isEqualTo(AppOpsManager.MODE_IGNORED)
    }

    @Test
    fun startRequestReadScreenContextActivity_verifyCopy() {
        addRoleHolder(RoleManager.ROLE_ASSISTANT, APP_PACKAGE_NAME)
        setAppOpMode(AppOpsManager.MODE_IGNORED)

        requestReadScreenContext()
        UiAutomatorUtils2.waitFindObject(By.text(TITLE))
        UiAutomatorUtils2.waitFindObject(By.text(DESCRIPTION))

        // Close dialog
        pressBack()
    }

    @Test
    fun startRequestReadScreenContextActivity_allow() {
        addRoleHolder(RoleManager.ROLE_ASSISTANT, APP_PACKAGE_NAME)
        setAppOpMode(AppOpsManager.MODE_IGNORED)

        requestReadScreenContext()
        respondToRequestAndWaitForResult(true)
        Truth.assertThat(getAppOpMode()).isEqualTo(AppOpsManager.MODE_ALLOWED)
        Truth.assertThat(getReadScreenContextRequestDeniedCount()).isEqualTo(0)
    }

    @Test
    fun startRequestReadScreenContextActivity_dontAllow() {
        addRoleHolder(RoleManager.ROLE_ASSISTANT, APP_PACKAGE_NAME)
        setAppOpMode(AppOpsManager.MODE_IGNORED)
        val expectedDenialCount = getReadScreenContextRequestDeniedCount() + 1

        requestReadScreenContext()
        respondToRequestAndWaitForResult(false)
        Truth.assertThat(getAppOpMode()).isEqualTo(AppOpsManager.MODE_IGNORED)
        Truth.assertThat(getReadScreenContextRequestDeniedCount()).isEqualTo(expectedDenialCount)
    }

    private fun getAppOpMode(): Int =
        SystemUtil.runWithShellPermissionIdentity<Int> {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
                assistantRoleHolderPackageUid,
                APP_PACKAGE_NAME,
            )
        }

    private fun setAppOpMode(mode: Int) =
        SystemUtil.runWithShellPermissionIdentity {
            appOpsManager.setUidMode(
                AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
                assistantRoleHolderPackageUid,
                mode,
            )
        }

    private fun getReadScreenContextRequestDeniedCount(): Int =
        readScreenContextDeniedCountManager.get()?.toIntOrNull() ?: 0

    private fun setReadScreenContextRequestDeniedCount(count: Int) =
        readScreenContextDeniedCountManager.set(count.toString())

    private fun saveRoleHolder() {
        val roleHolders = RoleManagerUtil.getRoleHolders(roleManager, RoleManager.ROLE_ASSISTANT)
        roleHolder = roleHolders.firstOrNull()

        if (roleHolder == APP_PACKAGE_NAME) {
            RoleManagerUtil.removeRoleHolder(
                roleManager,
                context,
                RoleManager.ROLE_ASSISTANT,
                APP_PACKAGE_NAME,
            )
            roleHolder = null
        }
    }

    private fun restoreRoleHolder() {
        RoleManagerUtil.removeRoleHolder(
            roleManager,
            context,
            RoleManager.ROLE_ASSISTANT,
            APP_PACKAGE_NAME,
        )

        roleHolder?.let {
            RoleManagerUtil.addRoleHolder(roleManager, context, RoleManager.ROLE_ASSISTANT, it)
        }

        assertIsRoleHolder(RoleManager.ROLE_ASSISTANT, APP_PACKAGE_NAME, false)
    }

    private fun addRoleHolder(roleName: String, packageName: String) {
        RoleManagerUtil.addRoleHolder(roleManager, context, roleName, packageName)
    }

    private fun removeRoleHolder(roleName: String, packageName: String) {
        RoleManagerUtil.removeRoleHolder(roleManager, context, roleName, packageName)
    }

    private fun assertIsRoleHolder(
        roleName: String,
        packageName: String,
        shouldBeRoleHolder: Boolean,
    ) {
        val packageNames = RoleManagerUtil.getRoleHolders(roleManager, roleName)

        if (shouldBeRoleHolder) {
            Truth.assertThat(packageNames).contains(packageName)
        } else {
            Truth.assertThat(packageNames).doesNotContain(packageName)
        }
    }

    private fun installPackage(apkPath: String, user: UserHandle = Process.myUserHandle()) {
        SystemUtil.runShellCommandOrThrow(
            "pm install -r --user " + user.getIdentifier() + " " + apkPath
        )
    }

    private fun uninstallPackage(packageName: String, user: UserHandle = Process.myUserHandle()) {
        SystemUtil.runShellCommand(
            "pm uninstall --user " + user.getIdentifier() + " " + packageName
        )
    }

    private fun pressBack() {
        UiAutomatorUtils2.getUiDevice().pressBack()
        UiAutomatorUtils2.getUiDevice().waitForIdle()
    }

    private fun requestReadScreenContext() {
        val intent =
            Intent()
                .setComponent(
                    ComponentName(APP_PACKAGE_NAME, APP_REQUEST_READ_SCREEN_CONTEXT_ACTIVITY_NAME)
                )
        activityRule.getActivity().startActivityToWaitForResult(intent)
        waitForFocus()
    }

    private fun respondToRequestAndWaitForResult(allow: Boolean) {
        if (allow) {
            UiAutomatorUtils2.waitFindObject(ALLOW_BUTTON_SELECTOR).click()
        } else {
            UiAutomatorUtils2.waitFindObject(DONT_ALLOW_BUTTON_SELECTOR).click()
        }
        val result: Pair<Int?, Intent?> = waitForResult()
        val expectedResult = if (allow) Activity.RESULT_OK else Activity.RESULT_CANCELED

        Truth.assertThat(result.first).isEqualTo(expectedResult)
    }

    private fun waitForFocus() {
        // Wait for any element to be focused, which indicates that the window is ready for input.
        UiAutomatorUtils2.getUiDevice()
            .wait(Until.hasObject(By.focused(true)), IDLE_LONG_TIMEOUT_MILLIS)
    }

    private fun waitForResult(): Pair<Int?, Intent?> {
        return activityRule.getActivity().waitForActivityResult(TIMEOUT_MILLIS)
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 15_000L
        private const val IDLE_LONG_TIMEOUT_MILLIS = 5_000L

        private const val APP_APK_PATH = "/data/local/tmp/cts-role/CtsRoleTestApp.apk"
        private const val APP_PACKAGE_NAME = "android.app.role.cts.app"
        private const val APP_REQUEST_READ_SCREEN_CONTEXT_ACTIVITY_NAME =
            "$APP_PACKAGE_NAME.RequestReadScreenContextActivity"

        private const val APP_LABEL = "CtsRoleTestApp"
        private const val TITLE = "Allow $APP_LABEL to use screen and app data?"
        private const val DESCRIPTION =
            "This assistant will be able to access info and content from the app open on your " +
                "screen"
        private val ALLOW_BUTTON_SELECTOR = By.text("Allow")
        private val DONT_ALLOW_BUTTON_SELECTOR = By.text("Don\u2019t allow")

        private val READ_SCREEN_CONTEXT_REQUEST_DENIED_COUNT =
            "read_screen_context_request_denied_count"

        @JvmStatic private val instrumentation = InstrumentationRegistry.getInstrumentation()
        @JvmStatic private val context = instrumentation.targetContext
    }
}
