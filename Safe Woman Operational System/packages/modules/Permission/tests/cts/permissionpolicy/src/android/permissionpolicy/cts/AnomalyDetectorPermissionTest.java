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

package android.permissionpolicy.cts;


import static android.Manifest.permission.CONFIGURE_ANOMALY_DETECTOR;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Process;
import android.os.profiling.anomaly.flags.Flags;
import android.platform.test.annotations.RequiresFlagsEnabled;
import android.platform.test.flag.junit.CheckFlagsRule;
import android.platform.test.flag.junit.DeviceFlagsValueProvider;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests to ensure only one app holds the
 * {@link android.Manifest.permission.CONFIGURE_ANOMALY_DETECTOR} permission.
 */
@RunWith(AndroidJUnit4.class)
@RequiresFlagsEnabled(Flags.FLAG_ANOMALY_DETECTOR_CORE_C)
public final class AnomalyDetectorPermissionTest {
    @Rule
    public final CheckFlagsRule mCheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule();

    @Test
    public void testAnomalyDetectorPermission() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        List<PackageInfo> packages = context.getPackageManager().getPackagesHoldingPermissions(
                new String[]{CONFIGURE_ANOMALY_DETECTOR}, 0);

        Set<Integer> uids = packages.stream()
                .filter(pkg -> pkg.applicationInfo != null
                        && pkg.applicationInfo.uid != Process.SHELL_UID)
                .map(pkg -> pkg.applicationInfo.uid)
                .collect(Collectors.toSet());

        assertThat(uids.size()).isAtMost(1);
    }
}
