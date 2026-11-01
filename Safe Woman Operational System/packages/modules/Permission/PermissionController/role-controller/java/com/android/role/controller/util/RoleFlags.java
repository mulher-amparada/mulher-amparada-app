/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.role.controller.util;

import android.os.Build;

import androidx.annotation.ChecksSdkIntAtLeast;

import com.android.modules.utils.build.SdkLevel;

/** Util class for getting shared feature flag check logic. */
public final class RoleFlags {
    private RoleFlags() { /* cannot be instantiated */ }

    /**
     * Returns whether profile group exclusive roles are available. Profile exclusive roles are
     * available on B+
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.BAKLAVA)
    public static boolean isProfileGroupExclusivityAvailable() {
        return SdkLevel.isAtLeastB() && com.android.permission.flags.Flags.crossUserRoleEnabled();
    }

    /**
     * Returns whether the role managers can manage {@link android.app.role.RoleManager#ROLE_NOTES}
     * via {@link android.app.role.RoleManager#getDefaultApplication(String)} and
     * {@link android.app.role.RoleManager#setDefaultApplication(String, String, int,
     * java.util.concurrent.Executor, java.util.function.Consumer)}.
     */
    public static boolean canManageDefaultNotesRolePackage() {
        return android.permission.flags.Flags.manageDefaultNotesRolePackageEnabled();
    }
}
