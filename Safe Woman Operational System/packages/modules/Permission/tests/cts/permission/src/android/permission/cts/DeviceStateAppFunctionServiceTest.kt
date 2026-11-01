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

package android.permission.cts

import android.Manifest
import android.app.appfunctions.AppFunctionException
import android.app.appfunctions.AppFunctionManager
import android.app.appfunctions.ExecuteAppFunctionRequest
import android.app.appfunctions.ExecuteAppFunctionResponse
import android.app.appsearch.GenericDocument
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import android.platform.test.annotations.AppModeFull
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.CheckFlagsRule
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.android.compatibility.common.util.DeviceConfigStateChangerRule
import com.android.compatibility.common.util.SystemUtil
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
@AppModeFull(reason = "Instant apps cannot access AppFunctionManager")
@RequiresFlagsEnabled(com.android.permission.flags.Flags.FLAG_APP_FUNCTION_SERVICE_ENABLED)
class DeviceStateAppFunctionServiceTest {

    private val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val packageManager = context.packageManager
    private val permissionControllerPackageName = packageManager.permissionControllerPackageName

    private val isTv = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    private val isWatch = packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
    private val isAutomotive = packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

    private lateinit var appFunctionManager: AppFunctionManager

    @get:Rule val checkFlagsRule: CheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    @get:Rule
    val setCancellationTimeoutRule: DeviceConfigStateChangerRule =
        DeviceConfigStateChangerRule(
            context,
            "appfunctions",
            "execute_app_function_cancellation_timeout_millis",
            "3000",
        )

    @get:Rule
    val setAgentAllowlistRule: DeviceConfigStateChangerRule =
        DeviceConfigStateChangerRule(
            context,
            "machine_learning",
            "allowlisted_app_functions_agents",
            context.packageName,
        )

    @Before
    fun setUp() {
        Assume.assumeFalse(isAutomotive)
        Assume.assumeFalse(isTv)
        Assume.assumeFalse(isWatch)

        setAppFunctionAllowlist(permissionControllerPackageName)
        appFunctionManager = context.getSystemService(AppFunctionManager::class.java)
        assertThat(appFunctionManager).isNotNull()
        installTestAppAndGrantPermission()
    }

    @After
    fun tearDown() {
        clearAppFunctionAllowlist()
        SystemUtil.runShellCommand("pm uninstall " + TEST_PACKAGE_NAME)
    }

    @Test
    fun onExecuteFunction_withInvalidFunctionIdentifier_returnsFunctionNotFoundError() = runTest {
        val request =
            ExecuteAppFunctionRequest.Builder(
                    permissionControllerPackageName,
                    "non-existent-function",
                )
                .build()

        uiAutomation.adoptShellPermissionIdentity(EXECUTE_APP_FUNCTIONS_PERMISSION)
        try {
            val response = executeAppFunctionAndWait(appFunctionManager, request)
            assertThat(response.isSuccess).isFalse()
            assertThat(response.appFunctionException().errorCode)
                .isEqualTo(AppFunctionException.ERROR_FUNCTION_NOT_FOUND)
        } finally {
            uiAutomation.dropShellPermissionIdentity()
        }
    }

    @Test
    fun onExecuteFunction_withValidIdentifier_returnsSuccessfulResponse() = runTest {
        val request =
            ExecuteAppFunctionRequest.Builder(
                    permissionControllerPackageName,
                    APP_FUNCTION_IDENTIFIER,
                )
                .build()

        uiAutomation.adoptShellPermissionIdentity(EXECUTE_APP_FUNCTIONS_PERMISSION)
        try {
            val response = executeAppFunctionAndWait(appFunctionManager, request)
            assertThat(response.isSuccess).isTrue()

            val document = response.getOrNull()!!.resultDocument
            assertThat(document.propertyNames)
                .containsExactly(ExecuteAppFunctionResponse.PROPERTY_RETURN_VALUE)

            val deviceStateScreens =
                document.getPropertyDocumentArray(
                    "${ExecuteAppFunctionResponse.PROPERTY_RETURN_VALUE}.perScreenDeviceStates"
                )
            assertThat(deviceStateScreens).isNotNull()

            val screenValidations = mutableMapOf<String, Boolean>()

            deviceStateScreens!!.forEach {
                val description = it.getPropertyString("description")!!

                if (description == "Permission Manager") {
                    assertPermissionManagerScreen(it, screenValidations)
                } else if (description.startsWith("Permission Manager:")) {
                    assertPermissionAppsScreen(it, screenValidations)
                } else if (description.startsWith("App Permissions Screen")) {
                    assertAppPermissionsScreen(it, screenValidations)
                } else if (description.contains("permission setting screen for a package")) {
                    assertAppPermissionSettingScreen(it, screenValidations)
                } else if (description.matches("""\w+\sPermission:\s.*""".toRegex())) {
                    assertAppPermissionGrantStateScreen(it, screenValidations)
                } else if (description == "Unused apps") {
                    assertUnusedAppsScreen(it, screenValidations)
                } else if (description.startsWith("Unused app details:")) {
                    assertUnusedAppLastUsageScreen(it, screenValidations)
                } else if (description == "Additional Permissions") {
                    assertAdditionalPermissionsScreen(it, screenValidations)
                } else if (description == "Default Apps") {
                    assertDefaultAppsScreen(it, screenValidations)
                } else if (description.matches("Default .* app".toRegex())) {
                    assertDefaultAppScreen(it, screenValidations)
                }
            }

            DEVICE_STATE_SCREENS.forEach { screen ->
                assertWithMessage(screen).that(screenValidations[screen]).isTrue()
            }
        } finally {
            uiAutomation.dropShellPermissionIdentity()
        }
    }

    private fun assertPermissionManagerScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val intent = Intent.parseUri(screen.getPropertyString("intentUri"), 0)
        assertThat(intent.action).isEqualTo(ACTION_MANAGE_PERMISSIONS)
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()
        screenValidations.putIfAbsent("PermissionManagerScreen", true)
    }

    private fun assertPermissionAppsScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val intent = Intent.parseUri(screen.getPropertyString("intentUri"), 0)
        assertThat(intent.action).isEqualTo(ACTION_MANAGE_PERMISSION_APPS)
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()
        assertThat(intent.getStringExtra(Intent.EXTRA_PERMISSION_GROUP_NAME))
            .isIn(SUPPORTED_PERMISSION_GROUPS)
        screenValidations.putIfAbsent("PermissionAppsScreen", true)
    }

    private fun assertAppPermissionsScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val description = screen.getPropertyString("description")
        assertThat(description)
            .contains("substitute \${package} with the package name in the intent uri")
        val intentUri = screen.getPropertyString("intentUri")
        val intent = Intent.parseUri(intentUri, 0)
        assertThat(intent.action).isEqualTo(ACTION_MANAGE_APP_PERMISSIONS)
        assertThat(intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)).isEqualTo("\${package}")
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()

        screenValidations.putIfAbsent("AppPermissionsScreen", true)
    }

    private fun assertAppPermissionSettingScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val description = screen.getPropertyString("description")
        assertThat(description)
            .contains("substitute \${package} with the package name in the intent uri")
        val intentUri = screen.getPropertyString("intentUri")
        val intent = Intent.parseUri(intentUri, 0)
        assertThat(intent.action).isEqualTo(ACTION_MANAGE_APP_PERMISSION)
        assertThat(intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)).isEqualTo("\${package}")
        assertThat(intent.getStringExtra(Intent.EXTRA_PERMISSION_GROUP_NAME)).isNotNull()
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()

        screenValidations.putIfAbsent("AppPermissionSettingScreen", true)
    }

    private fun assertAppPermissionGrantStateScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        if (screen.getPropertyString("description") == "Location Permission: $TEST_PACKAGE_NAME") {
            val deviceStateItem = screen.getPropertyDocumentArray("deviceStateItems")!![0]
            assertThat(deviceStateItem.getPropertyString("key"))
                .isEqualTo("location_permission_state")
            assertThat(deviceStateItem.getPropertyString("jsonValue")).isEqualTo("Always Allowed")
            assertThat(deviceStateItem.getPropertyDocument("name")!!.getPropertyString("english"))
                .isEqualTo("Location access for this app")
            screenValidations.putIfAbsent("AppPermissionGrantStateScreen", true)
        }
    }

    private fun assertUnusedAppsScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val intent = Intent.parseUri(screen.getPropertyString("intentUri"), 0)
        assertThat(intent.action).isEqualTo(ACTION_MANAGE_UNUSED_APPS)
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()
        screenValidations.putIfAbsent("UnusedAppsScreen", true)
    }

    private fun assertUnusedAppLastUsageScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val intent = Intent.parseUri(screen.getPropertyString("intentUri"), 0)
        assertThat(intent.action).isEqualTo(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()
        screenValidations.putIfAbsent("UnusedAppLastUsageScreen", true)
    }

    private fun assertAdditionalPermissionsScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val intent = Intent.parseUri(screen.getPropertyString("intentUri"), 0)
        assertThat(intent.action).isEqualTo(ACTION_ADDITIONAL_PERMISSIONS)
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()
        screenValidations.putIfAbsent("AdditionalPermissionsScreen", true)
    }

    private fun assertDefaultAppsScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val intent = Intent.parseUri(screen.getPropertyString("intentUri"), 0)
        assertThat(intent.action).isEqualTo(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()
        screenValidations.putIfAbsent("DefaultAppListScreen", true)
    }

    private fun assertDefaultAppScreen(
        screen: GenericDocument,
        screenValidations: MutableMap<String, Boolean>,
    ) {
        val intent = Intent.parseUri(screen.getPropertyString("intentUri"), 0)
        assertThat(intent.action).isEqualTo(ACTION_MANAGE_DEFAULT_APP)
        assertThat(intent.getStringExtra(Intent.EXTRA_ROLE_NAME)).isNotNull()
        assertThat(intent.getStringExtra(EXTRA_DEVICE_STATE_KEY)).isNotNull()

        val deviceStateItems = screen.getPropertyDocumentArray("deviceStateItems")
        assertThat(deviceStateItems).isNotNull()
        screenValidations.putIfAbsent("DefaultAppScreen", true)
    }

    private fun setAppFunctionAllowlist(targetPackage: String) {
        val packageInfo =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )
        val signatures = packageInfo.signingInfo?.signingCertificateHistory
        val certificate =
            if (!signatures.isNullOrEmpty()) {
                val digest =
                    MessageDigest.getInstance("SHA-256")
                        .digest(signatures.last().toByteArray())
                digest.joinToString("") { "%02X".format(it) }
            } else {
                throw IllegalStateException(
                    "No signatures found for package ${context.packageName}"
                )
            }
        val signedPackage = "${context.packageName}:$certificate"

        SystemUtil.runShellCommand("cmd app_function purge-allowlist-cache")
        SystemUtil.runShellCommand(
            buildString {
                append("cmd allowlist add-package-multimap")
                append(" ")
                append("$APP_FUNCTION_ALLOWLIST_TYPE")
                append(" ")
                append(signedPackage)
                append(" ")
                append(targetPackage)
            }
        )
    }

    private fun clearAppFunctionAllowlist() {
        SystemUtil.runShellCommand("cmd app_function purge-allowlist-cache")
        SystemUtil.runShellCommand(
            "cmd allowlist clear-shell-allowlist $APP_FUNCTION_ALLOWLIST_TYPE")
    }

    private fun installTestAppAndGrantPermission() {
        val output = SystemUtil.runShellCommandOrThrow("pm install -r -g " + TEST_APP)
        Assert.assertTrue(output.contains("Success"))

        uiAutomation.grantRuntimePermission(
            TEST_PACKAGE_NAME,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    private companion object {
        const val APP_FUNCTION_ALLOWLIST_TYPE = 2
        const val APP_FUNCTION_IDENTIFIER = "getPermissionsDeviceState"
        const val EXECUTE_APP_FUNCTIONS_PERMISSION = Manifest.permission.EXECUTE_APP_FUNCTIONS
        const val EXTRA_DEVICE_STATE_KEY = "com.android.permissioncontroller.devicestate.key"
        const val ACTION_MANAGE_PERMISSIONS =
            "com.android.permissioncontroller.devicestate.action.MANAGE_PERMISSIONS"
        const val ACTION_MANAGE_PERMISSION_APPS =
            "com.android.permissioncontroller.devicestate.action.MANAGE_PERMISSION_APPS"
        const val ACTION_MANAGE_APP_PERMISSIONS =
            "com.android.permissioncontroller.devicestate.action.MANAGE_APP_PERMISSIONS"
        const val ACTION_MANAGE_APP_PERMISSION =
            "com.android.permissioncontroller.devicestate.action.MANAGE_APP_PERMISSION"
        const val ACTION_MANAGE_UNUSED_APPS =
            "com.android.permissioncontroller.devicestate.action.MANAGE_UNUSED_APPS"
        const val ACTION_ADDITIONAL_PERMISSIONS =
            "com.android.permissioncontroller.devicestate.action.ADDITIONAL_PERMISSIONS"
        const val ACTION_MANAGE_DEFAULT_APP =
            "com.android.permissioncontroller.devicestate.action.MANAGE_DEFAULT_APP"
        val SUPPORTED_PERMISSION_GROUPS =
            setOf(
                Manifest.permission_group.LOCATION,
                Manifest.permission_group.CONTACTS,
                Manifest.permission_group.CALL_LOG,
            )
        const val TEST_PACKAGE_NAME = "android.permission.cts.appthataccesseslocation"
        const val TEST_APP =
            "/data/local/tmp/cts-permission/CtsAppThatAccessesLocationOnCommand.apk"

        // This should match the value in DeviceStateScreens.kt except UnusedAppLastUsageScreen
        // TODO: add test for UnusedAppLastUsageScreen
        val DEVICE_STATE_SCREENS =
            listOf(
                "PermissionManagerScreen",
                "PermissionAppsScreen",
                "AppPermissionsScreen",
                "AppPermissionSettingScreen",
                "AppPermissionGrantStateScreen",
                "UnusedAppsScreen",
                "AdditionalPermissionsScreen",
                "DefaultAppListScreen",
                "DefaultAppScreen",
            )

        suspend fun executeAppFunctionAndWait(
            manager: AppFunctionManager,
            request: ExecuteAppFunctionRequest,
        ): Result<ExecuteAppFunctionResponse> {
            return suspendCancellableCoroutine { continuation ->
                val cancellationSignal = CancellationSignal()
                continuation.invokeOnCancellation { cancellationSignal.cancel() }
                manager.executeAppFunction(
                    request,
                    Runnable::run,
                    cancellationSignal,
                    object : OutcomeReceiver<ExecuteAppFunctionResponse, AppFunctionException> {
                        override fun onResult(result: ExecuteAppFunctionResponse) {
                            continuation.resume(Result.success(result))
                        }

                        override fun onError(e: AppFunctionException) {
                            continuation.resume(Result.failure(e))
                        }
                    },
                )
            }
        }
    }
}

private fun <T> Result<T>.appFunctionException(): AppFunctionException =
    exceptionOrNull() as AppFunctionException
