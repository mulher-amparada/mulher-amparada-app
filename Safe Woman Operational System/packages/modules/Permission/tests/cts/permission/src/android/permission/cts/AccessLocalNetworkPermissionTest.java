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

package android.permission.cts;

import static com.google.common.truth.Truth.assertThat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.permission.flags.Flags;
import android.platform.test.annotations.AppModeFull;
import android.platform.test.annotations.RequiresFlagsEnabled;
import android.platform.test.flag.junit.CheckFlagsRule;
import android.platform.test.flag.junit.DeviceFlagsValueProvider;

import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@AppModeFull
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.CINNAMON_BUN, codeName = "CinnamonBun")
@RequiresFlagsEnabled(Flags.FLAG_ACCESS_LOCAL_NETWORK_PERMISSION_ENABLED)
public class AccessLocalNetworkPermissionTest {
    private static final String TMP_DIR = "/data/local/tmp/cts-permission/";
    private static final String TEST_APP_PKG =
            "android.permission.cts.appthatrequestssnearbydevicespermissions";
    private static final String APK_INTERNET_WIFI_36 = TMP_DIR
            + "CtsAppThatRequestsInternetAndNearbyWifiDevicesPermissions36.apk";
    private static final String APK_LNP_WIFI_37 = TMP_DIR
            + "CtsAppThatRequestsAccessLocalNetworkAndNearbyWifiDevicesPermissions37.apk";

    @Rule
    public final CheckFlagsRule mCheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule();

    @Before
    @After
    public void uninstallTestApp() {
        PermissionUtils.uninstallApp(TEST_APP_PKG);
    }

    @Test
    public void testPermissionRevokeOnPackageUpgrade() throws Exception {
        PermissionUtils.install(APK_INTERNET_WIFI_36);
        assertPermission(TEST_APP_PKG, Manifest.permission.ACCESS_LOCAL_NETWORK, true);

        PermissionUtils.install(APK_LNP_WIFI_37);
        assertPermission(TEST_APP_PKG, Manifest.permission.ACCESS_LOCAL_NETWORK, false);
    }

    @Test
    public void testUserFlagsResetOnPackageUpgrade() {
        PermissionUtils.install(APK_INTERNET_WIFI_36);
        PermissionUtils.setPermissionFlags(TEST_APP_PKG,
                Manifest.permission.NEARBY_WIFI_DEVICES,
                PackageManager.FLAG_PERMISSION_USER_SET | PackageManager.FLAG_PERMISSION_USER_FIXED,
                PackageManager.FLAG_PERMISSION_USER_SET
                        | PackageManager.FLAG_PERMISSION_USER_FIXED);
        assertUserFlagsState(TEST_APP_PKG, Manifest.permission.NEARBY_WIFI_DEVICES, true);

        PermissionUtils.install(APK_LNP_WIFI_37);
        assertUserFlagsState(TEST_APP_PKG, Manifest.permission.NEARBY_WIFI_DEVICES, false);
    }

    private void assertUserFlagsState(String packageName, String permission, boolean shouldBeSet) {
        int flags = PermissionUtils.getPermissionFlags(packageName, permission);
        int mask =
                PackageManager.FLAG_PERMISSION_USER_SET | PackageManager.FLAG_PERMISSION_USER_FIXED;
        int expectedValue = shouldBeSet ? mask : 0;
        assertThat(flags & mask).isEqualTo(expectedValue);
    }

    private void assertPermission(String packageName, String permission, boolean granted)
            throws Exception {
        assertThat(PermissionUtils.isPermissionGranted(packageName, permission)).isEqualTo(granted);
    }
}
