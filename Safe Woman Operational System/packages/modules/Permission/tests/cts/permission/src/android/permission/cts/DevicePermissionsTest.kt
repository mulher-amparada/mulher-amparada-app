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

package android.permission.cts

import android.Manifest
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE
import android.app.Instrumentation
import android.companion.virtual.VirtualDeviceManager.PERSISTENT_DEVICE_ID_DEFAULT
import android.companion.virtual.VirtualDeviceManager.VirtualDevice
import android.companion.virtual.VirtualDeviceParams
import android.companion.virtual.VirtualDeviceParams.DEVICE_POLICY_CUSTOM
import android.companion.virtual.VirtualDeviceParams.DEVICE_POLICY_DEFAULT
import android.companion.virtual.VirtualDeviceParams.POLICY_TYPE_AUDIO
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.FLAG_PERMISSION_ONE_TIME
import android.content.pm.PackageManager.FLAG_PERMISSION_USER_FIXED
import android.content.pm.PackageManager.FLAG_PERMISSION_USER_SENSITIVE_WHEN_GRANTED
import android.content.pm.PackageManager.FLAG_PERMISSION_USER_SET
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.UserHandle
import android.permission.PermissionManager
import android.platform.test.annotations.AppModeFull
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import android.virtualdevice.cts.common.VirtualDeviceRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.android.compatibility.common.util.SystemUtil.eventually
import com.android.compatibility.common.util.SystemUtil.runShellCommandOrThrow
import com.android.compatibility.common.util.SystemUtil.waitForBroadcasts
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@AppModeFull(reason = " cannot be accessed by instant apps")
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.VANILLA_ICE_CREAM, codeName = "VanillaIceCream")
class DevicePermissionsTest {
    private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    private val defaultDeviceContext = instrumentation.targetContext

    private lateinit var virtualDevice: VirtualDevice
    private lateinit var virtualDeviceContext: Context
    private lateinit var persistentDeviceId: String

    private lateinit var permissionManager: PermissionManager

    @get:Rule
    var mVirtualDeviceRule =
        VirtualDeviceRule.withAdditionalPermissions(
            Manifest.permission.GRANT_RUNTIME_PERMISSIONS,
            Manifest.permission.MANAGE_ONE_TIME_PERMISSION_SESSIONS,
            Manifest.permission.REVOKE_RUNTIME_PERMISSIONS,
            Manifest.permission.GET_RUNTIME_PERMISSIONS,
        )

    @Rule @JvmField val mCheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    @Before
    fun setup() {
        virtualDevice =
            mVirtualDeviceRule.createManagedVirtualDevice(
                // Without custom audio policy, the RECORD_AUDIO permission won't be device aware.
                VirtualDeviceParams.Builder()
                    .setDevicePolicy(POLICY_TYPE_AUDIO, DEVICE_POLICY_CUSTOM)
                    .build()
            )
        virtualDeviceContext = defaultDeviceContext.createDeviceContext(virtualDevice.deviceId)
        permissionManager = virtualDeviceContext.getSystemService(PermissionManager::class.java)!!
        persistentDeviceId = virtualDevice.persistentDeviceId!!
        runShellCommandOrThrow("pm install -r $TEST_APK")
    }

    @After
    fun cleanup() {
        runShellCommandOrThrow("pm uninstall $TEST_PACKAGE_NAME")
    }

    @Test
    fun virtualDeviceDefaultPolicy_deviceAwarePermissionFallsBackToDefaultDevice() {
        virtualDevice =
            mVirtualDeviceRule.createManagedVirtualDevice(
                // With default audio policy, the RECORD_AUDIO permission won't be device aware.
                VirtualDeviceParams.Builder()
                    .setDevicePolicy(POLICY_TYPE_AUDIO, DEVICE_POLICY_DEFAULT)
                    .build()
            )
        virtualDeviceContext = defaultDeviceContext.createDeviceContext(virtualDevice.deviceId)

        grantPermissionAndAssertGranted(DEVICE_AWARE_PERMISSION, defaultDeviceContext)
        assertPermission(DEVICE_AWARE_PERMISSION, PERMISSION_GRANTED, virtualDeviceContext)
    }

    @Test
    fun virtualDeviceCustomPolicy_deviceAwarePermissionGrantedOnVirtualDevice() {
        // When a device aware permission is granted on the default device, it's not automatically
        // granted on the virtual device.
        grantPermissionAndAssertGranted(DEVICE_AWARE_PERMISSION, defaultDeviceContext)
        assertPermission(DEVICE_AWARE_PERMISSION, PERMISSION_DENIED, virtualDeviceContext)

        grantPermissionAndAssertGranted(DEVICE_AWARE_PERMISSION, virtualDeviceContext)
    }

    @Test
    fun normalPermissionGrantedOnDefaultDevice_isGrantedOnVirtualDevice() {
        grantPermissionAndAssertGranted(NON_DEVICE_AWARE_PERMISSION, defaultDeviceContext)

        assertPermission(NON_DEVICE_AWARE_PERMISSION, PERMISSION_GRANTED, virtualDeviceContext)
    }

    @Test
    fun virtualDeviceCustomPolicy_deviceAwarePermissionIsRevoked() {
        grantPermissionAndAssertGranted(DEVICE_AWARE_PERMISSION, virtualDeviceContext)

        revokePermissionAndAssertDenied(DEVICE_AWARE_PERMISSION, virtualDeviceContext)
    }

    @Test
    fun normalPermissionRevokedFromVirtualDevice_isAlsoRevokedOnDefaultDevice() {
        grantPermissionAndAssertGranted(NON_DEVICE_AWARE_PERMISSION, defaultDeviceContext)
        assertPermission(NON_DEVICE_AWARE_PERMISSION, PERMISSION_GRANTED, virtualDeviceContext)
        // Revoke call from virtualDeviceContext should revoke for default device as well.
        revokePermissionAndAssertDenied(NON_DEVICE_AWARE_PERMISSION, virtualDeviceContext)
        assertPermission(NON_DEVICE_AWARE_PERMISSION, PERMISSION_DENIED, defaultDeviceContext)
    }

    @Test
    fun normalPermission_isInheritedOnVirtualDevice() {
        assertPermission(Manifest.permission.INTERNET, PERMISSION_GRANTED, virtualDeviceContext)
    }

    @Test
    fun signaturePermission_isInheritedOnVirtualDevice() {
        assertPermission(CUSTOM_SIGNATURE_PERMISSION, PERMISSION_GRANTED, virtualDeviceContext)
    }

    @Test
    fun virtualDeviceCustomPolicy_oneTimePermissionIsRevoked() {
        grantPermissionAndAssertGranted(DEVICE_AWARE_PERMISSION, virtualDeviceContext)
        virtualDeviceContext.packageManager.updatePermissionFlags(
            DEVICE_AWARE_PERMISSION,
            TEST_PACKAGE_NAME,
            FLAG_PERMISSION_ONE_TIME,
            FLAG_PERMISSION_ONE_TIME,
            UserHandle.of(virtualDeviceContext.userId),
        )

        permissionManager.startOneTimePermissionSession(
            TEST_PACKAGE_NAME,
            0,
            0,
            IMPORTANCE_FOREGROUND,
            IMPORTANCE_FOREGROUND_SERVICE,
        )
        eventually {
            assertPermission(DEVICE_AWARE_PERMISSION, PERMISSION_DENIED, virtualDeviceContext)
        }
    }

    @Test
    fun virtualDeviceCustomPolicy_revokeSelfPermissionOnKill_permissionIsRevoked() {
        grantPermissionAndAssertGranted(DEVICE_AWARE_PERMISSION, virtualDeviceContext)

        revokeSelfPermission(DEVICE_AWARE_PERMISSION, virtualDeviceContext)
        eventually {
            assertPermission(DEVICE_AWARE_PERMISSION, PERMISSION_DENIED, virtualDeviceContext)
        }
    }

    @Test
    fun usePersistentDeviceIdToRevokeDeviceAwarePermission_permissionIsRevoked() {
        permissionManager.grantRuntimePermission(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            persistentDeviceId,
        )

        assertThat(
                permissionManager.checkPermission(
                    DEVICE_AWARE_PERMISSION,
                    TEST_PACKAGE_NAME,
                    virtualDevice.persistentDeviceId!!,
                )
            )
            .isEqualTo(PERMISSION_GRANTED)

        assertThat(
                permissionManager.checkPermission(
                    DEVICE_AWARE_PERMISSION,
                    TEST_PACKAGE_NAME,
                    PERSISTENT_DEVICE_ID_DEFAULT,
                )
            )
            .isEqualTo(PERMISSION_DENIED)

        permissionManager.revokeRuntimePermission(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            persistentDeviceId,
            "test",
        )

        assertThat(
                permissionManager.checkPermission(
                    DEVICE_AWARE_PERMISSION,
                    TEST_PACKAGE_NAME,
                    virtualDevice.persistentDeviceId!!,
                )
            )
            .isEqualTo(PERMISSION_DENIED)
    }

    @Test
    fun updateAndGetPermissionFlagsByPersistentDeviceId() {
        val flagMask = FLAG_PERMISSION_USER_SET or FLAG_PERMISSION_USER_FIXED
        val flag = FLAG_PERMISSION_USER_SET

        assertThat(
                permissionManager.getPermissionFlags(
                    TEST_PACKAGE_NAME,
                    DEVICE_AWARE_PERMISSION,
                    persistentDeviceId,
                )
            )
            .isEqualTo(0)

        permissionManager.updatePermissionFlags(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            persistentDeviceId,
            flagMask,
            flag,
        )

        assertThat(
                permissionManager.getPermissionFlags(
                    TEST_PACKAGE_NAME,
                    DEVICE_AWARE_PERMISSION,
                    persistentDeviceId,
                )
            )
            .isEqualTo(FLAG_PERMISSION_USER_SET)
    }

    @Test
    fun permissionGrantedOnVirtualDevice_reflectedInGetAllPermissionStatesApi() {
        // Setting a flag explicitly so that the permission consistently stays in the state
        permissionManager.updatePermissionFlags(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            PERSISTENT_DEVICE_ID_DEFAULT,
            FLAG_PERMISSION_USER_SENSITIVE_WHEN_GRANTED,
            FLAG_PERMISSION_USER_SENSITIVE_WHEN_GRANTED,
        )

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, persistentDeviceId)
                    .isEmpty()
            )
            .isTrue()

        permissionManager.grantRuntimePermission(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            persistentDeviceId,
        )

        val permissionStateMap =
            permissionManager.getAllPermissionStates(TEST_PACKAGE_NAME, persistentDeviceId)
        assertThat(permissionStateMap.size).isEqualTo(1)
        assertThat(permissionStateMap[DEVICE_AWARE_PERMISSION]!!.isGranted).isTrue()
        assertThat(permissionStateMap[DEVICE_AWARE_PERMISSION]!!.flags).isEqualTo(0)

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, PERSISTENT_DEVICE_ID_DEFAULT)[
                        DEVICE_AWARE_PERMISSION]!!
                    .isGranted
            )
            .isFalse()

        permissionManager.revokeRuntimePermission(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            persistentDeviceId,
            "test",
        )

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, persistentDeviceId)
                    .contains(DEVICE_AWARE_PERMISSION)
            )
            .isFalse()
    }

    @Test
    fun setPermissionFlagOnVirtualDevice_reflectedInGetAllPermissionStatesApi() {
        val flagMask = FLAG_PERMISSION_USER_SET or FLAG_PERMISSION_USER_FIXED
        val flag = FLAG_PERMISSION_USER_SET

        assertThat(permissionManager.getAllPermissionStates(TEST_PACKAGE_NAME, persistentDeviceId))
            .isEmpty()

        permissionManager.updatePermissionFlags(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            persistentDeviceId,
            flagMask,
            flag,
        )

        assertThat(
                hasPermission(
                    permissionManager
                        .getAllPermissionStates(TEST_PACKAGE_NAME, persistentDeviceId)[
                            DEVICE_AWARE_PERMISSION]!!
                        .flags,
                    flag,
                )
            )
            .isTrue()

        assertThat(
                hasPermission(
                    permissionManager
                        .getAllPermissionStates(TEST_PACKAGE_NAME, persistentDeviceId)[
                            DEVICE_AWARE_PERMISSION]!!
                        .flags,
                    FLAG_PERMISSION_USER_FIXED,
                )
            )
            .isFalse()
    }

    @Test
    fun permissionGrantedOnDefaultDevice_reflectedInGetAllPermissionStatesApi() {
        // Setting a flag explicitly so that the permission consistently stays in the state upon
        // revoke
        permissionManager.updatePermissionFlags(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            PERSISTENT_DEVICE_ID_DEFAULT,
            FLAG_PERMISSION_USER_SENSITIVE_WHEN_GRANTED,
            FLAG_PERMISSION_USER_SENSITIVE_WHEN_GRANTED,
        )

        permissionManager.grantRuntimePermission(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            PERSISTENT_DEVICE_ID_DEFAULT,
        )

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, PERSISTENT_DEVICE_ID_DEFAULT)[
                        DEVICE_AWARE_PERMISSION]!!
                    .isGranted
            )
            .isTrue()

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, persistentDeviceId)
                    .contains(DEVICE_AWARE_PERMISSION)
            )
            .isFalse()

        permissionManager.revokeRuntimePermission(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            PERSISTENT_DEVICE_ID_DEFAULT,
            "test",
        )

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, PERSISTENT_DEVICE_ID_DEFAULT)[
                        DEVICE_AWARE_PERMISSION]!!
                    .isGranted
            )
            .isFalse()
    }

    @Test
    fun setPermissionFlagOnDefaultDevice_reflectedInGetAllPermissionStatesApi() {
        val flagMask = FLAG_PERMISSION_USER_SET or FLAG_PERMISSION_USER_FIXED
        val flag = FLAG_PERMISSION_USER_SET

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, PERSISTENT_DEVICE_ID_DEFAULT)
                    .contains(DEVICE_AWARE_PERMISSION)
            )
            .isFalse()

        permissionManager.updatePermissionFlags(
            TEST_PACKAGE_NAME,
            DEVICE_AWARE_PERMISSION,
            PERSISTENT_DEVICE_ID_DEFAULT,
            flagMask,
            flag,
        )

        assertThat(
                hasPermission(
                    permissionManager
                        .getAllPermissionStates(TEST_PACKAGE_NAME, PERSISTENT_DEVICE_ID_DEFAULT)[
                            DEVICE_AWARE_PERMISSION]!!
                        .flags,
                    flag,
                )
            )
            .isTrue()

        assertThat(
                hasPermission(
                    permissionManager
                        .getAllPermissionStates(TEST_PACKAGE_NAME, PERSISTENT_DEVICE_ID_DEFAULT)[
                            DEVICE_AWARE_PERMISSION]!!
                        .flags,
                    FLAG_PERMISSION_USER_FIXED,
                )
            )
            .isFalse()
    }

    @Test
    fun getAllPermissionStates_normalPermissionIsNotInherited() {
        permissionManager.grantRuntimePermission(
            TEST_PACKAGE_NAME,
            NON_DEVICE_AWARE_PERMISSION,
            PERSISTENT_DEVICE_ID_DEFAULT,
        )

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, PERSISTENT_DEVICE_ID_DEFAULT)[
                        NON_DEVICE_AWARE_PERMISSION]!!
                    .isGranted
            )
            .isTrue()

        assertThat(
                permissionManager
                    .getAllPermissionStates(TEST_PACKAGE_NAME, persistentDeviceId)
                    .contains(NON_DEVICE_AWARE_PERMISSION)
            )
            .isFalse()
    }

    private fun hasPermission(permissionFlags: Int, permissionBit: Int): Boolean =
        permissionFlags and permissionBit != 0

    private fun revokeSelfPermission(permissionName: String, context: Context) {
        val intent = Intent(PERMISSION_SELF_REVOKE_INTENT)
        intent.setClassName(TEST_PACKAGE_NAME, PERMISSION_SELF_REVOKE_RECEIVER)
        intent.putExtra("permissionName", permissionName)
        intent.putExtra("deviceID", context.deviceId)
        context.sendBroadcast(intent)
        waitForBroadcasts()
    }

    private fun grantPermissionAndAssertGranted(permissionName: String, context: Context) {
        context.packageManager.grantRuntimePermission(
            TEST_PACKAGE_NAME,
            permissionName,
            UserHandle.of(context.userId),
        )
        assertPermission(permissionName, PERMISSION_GRANTED, context)
    }

    private fun revokePermissionAndAssertDenied(permissionName: String, context: Context) {
        context.packageManager.revokeRuntimePermission(
            TEST_PACKAGE_NAME,
            permissionName,
            UserHandle.of(context.userId),
        )
        assertPermission(permissionName, PERMISSION_DENIED, context)
    }

    private fun assertPermission(permissionName: String, permissionState: Int, context: Context) {
        val uid = defaultDeviceContext.packageManager.getApplicationInfo(TEST_PACKAGE_NAME, 0).uid
        assertThat(context.checkPermission(permissionName, -1, uid)).isEqualTo(permissionState)
    }

    companion object {
        private const val TEST_PACKAGE_NAME = "android.permission.cts.appthatrequestpermission"
        private const val TEST_APK =
            "/data/local/tmp/cts-permission/CtsAppThatRequestsDevicePermissions.apk"

        private const val CUSTOM_SIGNATURE_PERMISSION =
            "android.permission.cts.CUSTOM_SIGNATURE_PERMISSION"

        private const val PERMISSION_SELF_REVOKE_INTENT =
            "android.permission.cts.appthatrequestpermission.REVOKE_SELF_PERMISSION"
        private const val PERMISSION_SELF_REVOKE_RECEIVER =
            "android.permission.cts.appthatrequestpermission.RevokeSelfPermissionReceiver"

        private const val DEVICE_AWARE_PERMISSION = Manifest.permission.RECORD_AUDIO
        private const val NON_DEVICE_AWARE_PERMISSION = Manifest.permission.READ_CONTACTS
    }
}
