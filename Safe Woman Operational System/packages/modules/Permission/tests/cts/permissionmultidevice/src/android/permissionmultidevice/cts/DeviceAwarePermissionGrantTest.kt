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

package android.permissionmultidevice.cts

import android.Manifest
import android.app.Instrumentation
import android.companion.virtual.VirtualDeviceManager
import android.companion.virtual.VirtualDeviceParams
import android.companion.virtual.VirtualDeviceParams.DEVICE_POLICY_CUSTOM
import android.companion.virtual.VirtualDeviceParams.DEVICE_POLICY_DEFAULT
import android.companion.virtual.VirtualDeviceParams.POLICY_TYPE_CAMERA
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_RESULT_RECEIVER
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ACTION_REQUEST_PERMISSIONS
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.os.Bundle
import android.os.RemoteCallback
import android.permission.PermissionManager
import android.permission.flags.Flags
import android.permissionmultidevice.cts.PackageManagementUtils.installPackage
import android.permissionmultidevice.cts.PackageManagementUtils.uninstallPackage
import android.permissionmultidevice.cts.UiAutomatorUtils.click
import android.permissionmultidevice.cts.UiAutomatorUtils.findTextForView
import android.permissionmultidevice.cts.UiAutomatorUtils.waitFindObject
import android.platform.test.annotations.AppModeFull
import android.platform.test.annotations.RequiresFlagsEnabled
import android.provider.Settings
import android.view.Display
import android.virtualdevice.cts.common.VirtualDeviceRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import com.android.compatibility.common.util.DisableAnimationRule
import com.android.compatibility.common.util.SystemUtil
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA, codeName = "Baklava")
@AppModeFull(reason = "VirtualDeviceManager cannot be accessed by instant apps")
open class DeviceAwarePermissionGrantTest {
    private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    private val defaultDeviceContext = instrumentation.targetContext
    private lateinit var defaultDeviceName: String
    private lateinit var virtualDeviceManager: VirtualDeviceManager
    private lateinit var virtualDevice: VirtualDeviceManager.VirtualDevice
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var virtualDeviceName: String
    private val permissionManager =
        defaultDeviceContext.getSystemService(PermissionManager::class.java)!!

    @get:Rule
    var virtualDeviceRule: VirtualDeviceRule =
        VirtualDeviceRule.withAdditionalPermissions(Manifest.permission.GRANT_RUNTIME_PERMISSIONS)

    @get:Rule val disableAnimationRule = DisableAnimationRule()

    @Before
    fun setup() {
        assumeFalse(PermissionUtils.isAutomotive(defaultDeviceContext))
        assumeFalse(PermissionUtils.isTv(defaultDeviceContext))
        assumeFalse(PermissionUtils.isWatch(defaultDeviceContext))

        installPackage(APP_APK_PATH_STREAMING)
        virtualDeviceManager =
            defaultDeviceContext.getSystemService(VirtualDeviceManager::class.java)!!

        defaultDeviceName =
            Settings.Global.getString(
                defaultDeviceContext.contentResolver,
                Settings.Global.DEVICE_NAME,
            )
    }

    @After
    fun cleanup() {
        uninstallPackage(APP_PACKAGE_NAME, requireSuccess = false)
        Thread.sleep(2000)
    }

    private fun createVirtualDevice(cameraPolicy: Int = DEVICE_POLICY_DEFAULT) {
        virtualDevice =
            virtualDeviceRule.createManagedVirtualDevice(
                VirtualDeviceParams.Builder()
                    .setDevicePolicy(POLICY_TYPE_CAMERA, cameraPolicy)
                    .build()
            )
        virtualDisplay =
            virtualDeviceRule.createManagedVirtualDisplay(
                virtualDevice,
                VirtualDeviceRule.createTrustedVirtualDisplayConfigBuilder(),
            )!!
        virtualDeviceName =
            virtualDeviceManager.getVirtualDevice(virtualDevice.deviceId)!!.displayName.toString()
    }

    @Test
    fun deviceAwarePermission_onHost_requestHostPermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = DEVICE_ID_DEFAULT,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = false,
        )
    }

    @Test
    fun deviceAwarePermission_onHost_withRemotePermission_requestHostPermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = true,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = DEVICE_ID_DEFAULT,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermission_onHost_requestPermissionWithoutDeviceId() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = DEVICE_ID_INVALID,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = false,
        )
    }

    @Test
    fun deviceAwarePermission_onHost_withRemotePermission_requestPermissionWithoutDeviceId() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = true,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = DEVICE_ID_INVALID,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermission_onHost_requestRemotePermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = virtualDevice.deviceId,
            dialogDeviceName = virtualDevice.deviceId,
            expectPermissionGrantedOnDefaultDevice = false,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermission_onHost_withHostPermission_requestRemotePermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = true,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = virtualDevice.deviceId,
            dialogDeviceName = virtualDevice.deviceId,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermission_onRemote_requestRemotePermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = virtualDevice.deviceId,
            dialogDeviceName = virtualDevice.deviceId,
            expectPermissionGrantedOnDefaultDevice = false,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermission_onRemote_withHostPermission_requestRemotePermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = true,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = virtualDevice.deviceId,
            dialogDeviceName = virtualDevice.deviceId,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermission_onRemote_requestPermissionWithoutDeviceId() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = DEVICE_ID_INVALID,
            dialogDeviceName = virtualDevice.deviceId,
            expectPermissionGrantedOnDefaultDevice = false,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermission_onRemote_withHostPermission_requestPermissionWithoutDeviceId() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = true,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = DEVICE_ID_INVALID,
            dialogDeviceName = virtualDevice.deviceId,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun deviceAwarePermission_onRemote_requestHostPermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = DEVICE_ID_DEFAULT,
            dialogDeviceName = DEVICE_ID_DEFAULT,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = false,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun deviceAwarePermission_onRemote_withRemotePermission_requestHostPermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_CUSTOM)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = true,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = DEVICE_ID_DEFAULT,
            dialogDeviceName = DEVICE_ID_DEFAULT,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermissionWithoutCapability_onHost_requestHostPermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_DEFAULT)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = DEVICE_ID_DEFAULT,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun deviceAwarePermissionWithoutCapability_onHost_requestPermissionWithoutDeviceId() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_DEFAULT)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = DEVICE_ID_INVALID,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun deviceAwarePermissionWithoutCapability_onHost_requestRemotePermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_DEFAULT)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = virtualDevice.deviceId,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun deviceAwarePermissionWithoutCapability_onRemote_requestRemotePermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_DEFAULT)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = virtualDevice.deviceId,
            dialogDeviceName = DEVICE_ID_DEFAULT,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun deviceAwarePermissionWithoutCapability_onRemote_requestPermissionWithoutDeviceId() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_DEFAULT)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = DEVICE_ID_INVALID,
            dialogDeviceName = DEVICE_ID_DEFAULT,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun deviceAwarePermissionWithoutCapability_onRemote_requestHostPermission() {
        createVirtualDevice(cameraPolicy = DEVICE_POLICY_DEFAULT)
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.CAMERA,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = DEVICE_ID_DEFAULT,
            dialogDeviceName = DEVICE_ID_DEFAULT,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun nonDeviceAwarePermission_onHost_requestHostPermission() {
        createVirtualDevice()
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.READ_CONTACTS,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = DEVICE_ID_DEFAULT,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @Test
    fun nonDeviceAwarePermission_onHost_requestPermissionWithoutDeviceId() {
        createVirtualDevice()
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.READ_CONTACTS,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = DEVICE_ID_INVALID,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun nonDeviceAwarePermission_onHost_requestRemotePermission() {
        createVirtualDevice()
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.READ_CONTACTS,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = DEVICE_ID_DEFAULT,
            requestingForDevice = virtualDevice.deviceId,
            dialogDeviceName = DEVICE_ID_INVALID,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun nonDeviceAwarePermission_onRemote_requestRemotePermission() {
        createVirtualDevice()
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.READ_CONTACTS,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = virtualDevice.deviceId,
            dialogDeviceName = DEVICE_ID_DEFAULT,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun nonDeviceAwarePermission_onRemote_requestPermissionWithoutDeviceId() {
        createVirtualDevice()
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.READ_CONTACTS,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = DEVICE_ID_INVALID,
            dialogDeviceName = DEVICE_ID_DEFAULT,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    @RequiresFlagsEnabled(Flags.FLAG_ALLOW_HOST_PERMISSION_DIALOGS_ON_VIRTUAL_DEVICES)
    @Test
    fun nonDeviceAwarePermission_onRemote_requestHostPermission() {
        createVirtualDevice()
        testMultiDevicePermissionGrant(
            permission = Manifest.permission.READ_CONTACTS,
            initialDefaultDevicePermission = false,
            initialVirtualDevicePermission = false,
            requestingFromDevice = virtualDevice.deviceId,
            requestingForDevice = DEVICE_ID_DEFAULT,
            dialogDeviceName = DEVICE_ID_DEFAULT,
            expectPermissionGrantedOnDefaultDevice = true,
            expectPermissionGrantedOnVirtualDevice = true,
        )
    }

    private fun testMultiDevicePermissionGrant(
        permission: String,
        initialDefaultDevicePermission: Boolean,
        initialVirtualDevicePermission: Boolean,
        requestingFromDevice: Int,
        requestingForDevice: Int,
        dialogDeviceName: Int,
        expectPermissionGrantedOnDefaultDevice: Boolean,
        expectPermissionGrantedOnVirtualDevice: Boolean,
    ) {
        if (initialDefaultDevicePermission) {
            instrumentation.uiAutomation.grantRuntimePermission(APP_PACKAGE_NAME, permission)
        }
        if (initialVirtualDevicePermission) {
            grantRuntimePermissionOnVirtualDevice(permission)
        }
        assertAppHasPermissionForDevice(
            DEVICE_ID_DEFAULT,
            permission,
            initialDefaultDevicePermission,
        )
        assertAppHasPermissionForDevice(
            virtualDevice.deviceId,
            permission,
            initialVirtualDevicePermission,
        )

        val displayId =
            if (requestingFromDevice == DEVICE_ID_DEFAULT) {
                Display.DEFAULT_DISPLAY
            } else {
                virtualDisplay.display.displayId
            }

        val future = requestPermissionOnDevice(displayId, requestingForDevice, permission)

        if (dialogDeviceName != DEVICE_ID_INVALID) {
            val expectedDeviceNameInDialog =
                if (dialogDeviceName == DEVICE_ID_DEFAULT) {
                    defaultDeviceName
                } else {
                    virtualDeviceName
                }
            assertPermissionMessageContainsDeviceName(displayId, expectedDeviceNameInDialog)
        } else {
            assertPermissionMessageDoesNotContainDeviceName(displayId)
        }

        // Click the allow button in the dialog to grant permission
        val allowButton =
            if (permission == Manifest.permission.CAMERA) ALLOW_BUTTON_FOREGROUND else ALLOW_BUTTON
        SystemUtil.eventually { click(By.displayId(displayId).res(allowButton)) }

        // Validate permission grant result returned from callback
        val grantPermissionResult = future.get(TIMEOUT, TimeUnit.MILLISECONDS)
        assertThat(
                grantPermissionResult.getStringArray(
                    TestConstants.PERMISSION_RESULT_KEY_PERMISSIONS
                )
            )
            .isEqualTo(arrayOf(permission))
        assertThat(
                grantPermissionResult.getIntArray(TestConstants.PERMISSION_RESULT_KEY_GRANT_RESULTS)
            )
            .isEqualTo(arrayOf(PackageManager.PERMISSION_GRANTED).toIntArray())

        val expectedPermissionResultDeviceId =
            if (requestingForDevice == DEVICE_ID_INVALID) {
                requestingFromDevice
            } else {
                requestingForDevice
            }
        assertThat(grantPermissionResult.getInt(TestConstants.PERMISSION_RESULT_KEY_DEVICE_ID))
            .isEqualTo(expectedPermissionResultDeviceId)

        // Validate whether permission is granted as expected
        assertAppHasPermissionForDevice(
            DEVICE_ID_DEFAULT,
            permission,
            expectPermissionGrantedOnDefaultDevice,
        )
        assertAppHasPermissionForDevice(
            virtualDevice.deviceId,
            permission,
            expectPermissionGrantedOnVirtualDevice,
        )
    }

    private fun requestPermissionOnDevice(
        displayId: Int,
        targetDeviceId: Int,
        permission: String,
    ): CompletableFuture<Bundle> {
        val future = CompletableFuture<Bundle>()
        val callback = RemoteCallback { result: Bundle? -> future.complete(result) }
        val permissions = mutableListOf(permission).toTypedArray()
        val intent =
            Intent()
                .setComponent(
                    ComponentName(APP_PACKAGE_NAME, "$APP_PACKAGE_NAME.RequestPermissionActivity")
                )
                .putExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_DEVICE_ID, targetDeviceId)
                .putExtra(PackageManager.EXTRA_REQUEST_PERMISSIONS_NAMES, permissions)
                .putExtra(EXTRA_RESULT_RECEIVER, callback)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        virtualDeviceRule.sendIntentToDisplay(intent, displayId)
        virtualDeviceRule.waitAndAssertActivityResumed(getPermissionDialogComponentName())

        return future
    }

    private fun assertPermissionMessageContainsDeviceName(displayId: Int, deviceName: String) {
        waitFindObject(By.displayId(displayId).res(PERMISSION_MESSAGE_ID))
        val text = findTextForView(By.displayId(displayId).res(PERMISSION_MESSAGE_ID))
        assertThat(text).contains(deviceName)
    }

    private fun assertPermissionMessageDoesNotContainDeviceName(displayId: Int) {
        waitFindObject(By.displayId(displayId).res(PERMISSION_MESSAGE_ID))
        val text = findTextForView(By.displayId(displayId).res(PERMISSION_MESSAGE_ID))
        assertThat(text).doesNotContain(virtualDeviceName)
        assertThat(text).doesNotContain(defaultDeviceName)
    }

    private fun assertAppHasPermissionForDevice(
        deviceId: Int,
        permission: String,
        expectPermissionGranted: Boolean,
    ) {
        val checkPermissionResult =
            defaultDeviceContext
                .createDeviceContext(deviceId)
                .packageManager
                .checkPermission(permission, APP_PACKAGE_NAME)

        if (expectPermissionGranted) {
            Assert.assertEquals(PackageManager.PERMISSION_GRANTED, checkPermissionResult)
        } else {
            Assert.assertEquals(PackageManager.PERMISSION_DENIED, checkPermissionResult)
        }
    }

    private fun getPermissionDialogComponentName(): ComponentName {
        val intent = Intent(ACTION_REQUEST_PERMISSIONS)
        intent.setPackage(defaultDeviceContext.packageManager.getPermissionControllerPackageName())
        return intent.resolveActivity(defaultDeviceContext.packageManager)
    }

    private fun grantRuntimePermissionOnVirtualDevice(permission: String) {
        permissionManager.grantRuntimePermission(
            APP_PACKAGE_NAME,
            permission,
            virtualDevice.persistentDeviceId!!,
        )
    }

    companion object {
        const val APK_DIRECTORY = "/data/local/tmp/cts-permissionmultidevice"
        const val APP_APK_PATH_STREAMING = "${APK_DIRECTORY}/CtsAccessRemoteDeviceCamera.apk"
        const val APP_PACKAGE_NAME = "android.permissionmultidevice.cts.accessremotedevicecamera"
        const val PERMISSION_MESSAGE_ID = "com.android.permissioncontroller:id/permission_message"
        const val ALLOW_BUTTON_FOREGROUND =
            "com.android.permissioncontroller:id/permission_allow_foreground_only_button"
        const val ALLOW_BUTTON = "com.android.permissioncontroller:id/permission_allow_button"
        const val DEVICE_ID_DEFAULT = Context.DEVICE_ID_DEFAULT
        const val DEVICE_ID_INVALID = Context.DEVICE_ID_INVALID
        const val PERSISTENT_DEVICE_ID_DEFAULT = VirtualDeviceManager.PERSISTENT_DEVICE_ID_DEFAULT
        const val TIMEOUT = 5000L
    }
}
