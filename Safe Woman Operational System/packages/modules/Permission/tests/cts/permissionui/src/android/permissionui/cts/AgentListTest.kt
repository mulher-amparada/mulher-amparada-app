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

import android.app.appfunctions.AppFunctionManager.ACTION_MANAGE_APP_FUNCTION_ACCESS
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.permission.flags.Flags
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.filters.SdkSuppress
import androidx.test.uiautomator.By
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
class AgentListTest : BaseUsePermissionTest() {
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

    @Before
    fun setup() {
        assumeFalse(isAutomotive)
        assumeFalse(isTv)
        assumeFalse(isWatch)
    }

    @After
    fun uninstallAgentApp() {
        uninstallPackage(AGENT_APP_PACKAGE_NAME, requireSuccess = false)
    }

    @Test
    fun startActivityWithIntent_showTitle() {
        startAppFunctionAgentListActivity()

        try {
            findView(By.descContains(APP_FUNCTION_AGENT_LIST_TITLE), true)
        } finally {
            pressBack()
        }
    }

    @Test
    fun startActivityWithIntent_showSummary() {
        startAppFunctionAgentListActivity()

        try {
            findView(By.textContains(APP_FUNCTION_AGENT_LIST_SUMMARY), true)
        } finally {
            pressBack()
        }
    }

    @Test
    fun startActivityWithIntent_dontShowInvalidAgent() {
        startAppFunctionAgentListActivity()

        try {
            findView(By.textContains(AGENT_APP_LABEL), false)
        } finally {
            pressBack()
        }
    }

    @Test
    fun startActivityWithIntent_showAgent() {
        installPackage(AGENT_APP_APK_PATH)
        startAppFunctionAgentListActivity()

        try {
            findView(By.textContains(AGENT_APP_LABEL), true)
        } finally {
            pressBack()
        }
    }

    @Test
    fun agentAppAddedAndRemoved_uiStateUpdated() {
        startAppFunctionAgentListActivity()

        try {
            // Verify agent is not shown initially
            findView(By.textContains(AGENT_APP_LABEL), false)

            // Install agent and verify it's shown
            installPackage(AGENT_APP_APK_PATH)
            eventually { findView(By.textContains(AGENT_APP_LABEL), true) }

            // Uninstall agent and verify it's not shown
            uninstallPackage(AGENT_APP_PACKAGE_NAME)
            eventually { findView(By.textContains(AGENT_APP_LABEL), false) }
        } finally {
            pressBack()
        }
    }

    /** Starts activity with intent [ACTION_MANAGE_APP_FUNCTION_ACCESS]. */
    private fun startAppFunctionAgentListActivity() {
        doAndWaitForWindowTransition {
            runWithShellPermissionIdentity {
                context.startActivity(
                    Intent(ACTION_MANAGE_APP_FUNCTION_ACCESS).apply {
                        addFlags(FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
        }
    }

    companion object {
        const val AGENT_APP_APK_PATH = "$APK_DIRECTORY/CtsAgentApp.apk"
        const val AGENT_APP_PACKAGE_NAME = "android.permissionui.cts.appfunctions.agent"
        const val AGENT_APP_LABEL = "CtsAgentApp"

        private const val APP_FUNCTION_AGENT_LIST_TITLE = "Agents"
        private const val APP_FUNCTION_AGENT_LIST_SUMMARY =
            "Manage access for agents on this device"
    }
}
