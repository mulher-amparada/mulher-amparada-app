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
package android.permissionui.cts

import android.app.appfunctions.AppFunctionManager
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_MASK_ALL
import android.app.appfunctions.AppFunctionManager.ACTION_MANAGE_AGENT_APP_FUNCTION_ACCESS
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.permission.flags.Flags
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.filters.SdkSuppress
import androidx.test.uiautomator.By
import com.android.compatibility.common.util.SystemUtil.callWithShellPermissionIdentity
import com.android.compatibility.common.util.SystemUtil.eventually
import com.android.compatibility.common.util.SystemUtil.runWithShellPermissionIdentity
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// TODO(b/424004217): Update this to the correct version code
/** Tests the UI that displays the app function agents list. */
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
@RequiresFlagsEnabled(
    Flags.FLAG_APP_FUNCTION_ACCESS_API_ENABLED,
    Flags.FLAG_APP_FUNCTION_ACCESS_UI_ENABLED,
)
class AgentAccessTest : BaseUsePermissionTest() {
    @get:Rule val checkFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    // Uncomment this once the agent allowlist can be updated. Currently causing a failure due to
    // DeviceConfigStateChangerRule being evaluated when required flags are disabled
    /* @get:Rule
    val setAgentAllowlistRule: DeviceConfigStateChangerRule =
        DeviceConfigStateChangerRule(
            context,
            "machine_learning",
            "allowlisted_app_functions_agents",
            "android.permissionui.cts.appfunctions.agent",
        ) */

    private val appFunctionManager = context.getSystemService(AppFunctionManager::class.java)!!

    @Before
    fun setup() {
        assumeFalse(isAutomotive)
        assumeFalse(isTv)
        assumeFalse(isWatch)

        installPackage(AGENT_APP_APK_PATH)
        installPackage(TARGET_APP_APK_PATH)

        setAppFunctionFlags(0, AGENT_APP_PACKAGE_NAME, TARGET_APP_PACKAGE_NAME)
    }

    @After
    fun cleanup() {
        setAppFunctionFlags(0, AGENT_APP_PACKAGE_NAME, TARGET_APP_PACKAGE_NAME)

        uninstallPackage(AGENT_APP_PACKAGE_NAME, requireSuccess = false)
        uninstallPackage(TARGET_APP_PACKAGE_NAME, requireSuccess = false)
    }

    @Test
    fun startActivityWithIntent_showTitle() {
        startAppFunctionAgentAccessActivity()

        try {
            findView(By.descContains(APP_FUNCTION_AGENT_ACCESS_TITLE), true)
        } finally {
            pressBack()
        }
    }

    @Test
    fun startActivityWithIntent_showAgentAppLabel() {
        startAppFunctionAgentAccessActivity()

        try {
            findView(By.textContains(AGENT_APP_LABEL), true)
        } finally {
            pressBack()
        }
    }

    @Test
    fun startActivityWithIntent_showSummary() {
        startAppFunctionAgentAccessActivity()

        try {
            findView(By.textContains(APP_FUNCTION_AGENT_ACCESS_SUMMARY), true)
        } finally {
            pressBack()
        }
    }

    @Test
    fun clickTargetApp_startsManageAccess() {
        startAppFunctionAgentAccessActivity()

        // Trigger grant/revoke access from setting entry
        try {
            doAndWaitForWindowTransition { click(By.textContains(TARGET_APP_LABEL)) }

            // Manage Access title in collapsing toolbar requires descContains
            findView(By.descContains(APP_FUNCTION_MANAGE_ACCESS_TITLE), true)
        } finally {
            pressBack()
            pressBack()
        }
    }

    @Test
    fun targetAppAddedAndRemoved_uiStateUpdated() {
        startAppFunctionAgentAccessActivity()

        try {
            // Verify target is shown initially
            findView(By.textContains(TARGET_APP_LABEL), true)

            // Uninstall target and verify it's not shown
            uninstallPackage(TARGET_APP_PACKAGE_NAME)
            eventually { findView(By.textContains(TARGET_APP_LABEL), false) }

            // Install target and verify it's shown
            installPackage(TARGET_APP_APK_PATH)
            eventually { findView(By.textContains(TARGET_APP_LABEL), true) }
        } finally {
            pressBack()
        }
    }

    /** Starts activity with intent [ACTION_MANAGE_AGENT_APP_FUNCTION_ACCESS]. */
    private fun startAppFunctionAgentAccessActivity() {
        doAndWaitForWindowTransition {
            runWithShellPermissionIdentity {
                context.startActivity(
                    Intent(ACTION_MANAGE_AGENT_APP_FUNCTION_ACCESS).apply {
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                        putExtra(Intent.EXTRA_PACKAGE_NAME, AGENT_APP_PACKAGE_NAME)
                    }
                )
            }
        }
    }

    private fun setAppFunctionFlags(
        flags: Int,
        agentPackageName: String,
        targetPackageName: String,
    ) = callWithShellPermissionIdentity {
        appFunctionManager.updateAccessFlags(
            agentPackageName,
            targetPackageName,
            ACCESS_FLAG_MASK_ALL,
            flags,
        )
    }

    companion object {
        private const val AGENT_APP_APK_PATH = "$APK_DIRECTORY/CtsAgentApp.apk"
        private const val TARGET_APP_APK_PATH = "$APK_DIRECTORY/CtsTargetApp.apk"
        private const val AGENT_APP_PACKAGE_NAME = "android.permissionui.cts.appfunctions.agent"
        private const val TARGET_APP_PACKAGE_NAME = "android.permissionui.cts.appfunctions.target"
        private const val AGENT_APP_LABEL = "CtsAgentApp"
        private const val TARGET_APP_LABEL = "CtsTargetApp"

        private const val APP_FUNCTION_AGENT_ACCESS_TITLE = "App access"
        private const val APP_FUNCTION_AGENT_ACCESS_SUMMARY =
            "Apps and settings this agent can access to get info and take actions for you"

        private const val APP_FUNCTION_MANAGE_ACCESS_TITLE = "Access to $TARGET_APP_LABEL"
    }
}
