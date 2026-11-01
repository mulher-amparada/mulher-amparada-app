/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.permissioncontroller.common.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.modules.utils.build.SdkLevel;
import com.android.permissioncontroller.DeviceUtils;
import com.android.permissioncontroller.R;
import com.android.settingslib.collapsingtoolbar.EdgeToEdgeUtils;
import com.android.settingslib.collapsingtoolbar.SettingsTransitionActivity;
import com.android.settingslib.widget.ExpressiveDesignEnabledProvider;
import com.android.settingslib.widget.SettingsThemeHelper;
import com.android.settingslib.widget.theme.flags.Flags;

/**
 * Base class for settings activities.
 */
public class SettingsActivity extends SettingsTransitionActivity implements
        ExpressiveDesignEnabledProvider {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (DeviceUtils.isAuto(this)) {
            // Automotive relies on a different theme.
            setTheme(R.style.CarSettings);
        } else if (SettingsThemeHelper.isExpressiveTheme(this)) {
            setTheme(R.style.Theme_PermissionController_Settings_Expressive_FilterTouches);
        }
        enableEdgeToEdge(this);

        super.onCreate(savedInstanceState);

        getWindow().addSystemFlags(
                WindowManager.LayoutParams.SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS);
    }

    @Override
    protected boolean isSettingsTransitionEnabled() {
        return super.isSettingsTransitionEnabled() && !(DeviceUtils.isAuto(this)
                || DeviceUtils.isTelevision(this) || DeviceUtils.isWear(this));
    }

    @Override
    public boolean isExpressiveDesignEnabled() {
        return SdkLevel.isAtLeastB() && DeviceUtils.isHandheld()
                && Flags.isExpressiveDesignEnabled();
    }

    /**
     * Enable EdgeToEdge. For C+, it will also useThemeColors.
     */
    public static void enableEdgeToEdge(@NonNull ComponentActivity activity) {
        if (isAtLeastC()) {
            EdgeToEdgeUtils.enable(activity, true);
        } else {
            EdgeToEdgeUtils.enable(activity);
        }
    }

    /**
     * This is the private sdk check for CinnamonBun for pre-SDK-finalization.
     */
    private static boolean isAtLeastC() {
        return Build.VERSION.SDK_INT >= 37
                || (Build.VERSION.SDK_INT == 36 && isAtLeastPreReleaseCodename("CinnamonBun"));
    }

    private static boolean isAtLeastPreReleaseCodename(@NonNull String codename) {
        // Special case "REL", which means the build is not a pre-release build.
        if ("REL".equals(Build.VERSION.CODENAME)) {
            return false;
        }

        // Otherwise lexically compare them. Return true if the build codename is equal to or
        // greater than the requested codename.
        return Build.VERSION.CODENAME.compareTo(codename) >= 0;
    }
}
