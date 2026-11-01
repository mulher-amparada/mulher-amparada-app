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

package com.android.role.controller.behavior.v37;

import android.app.NotificationManager;
import android.content.Context;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import com.android.role.controller.model.Role;
import com.android.role.controller.model.RoleBehavior;
import com.android.role.controller.util.UserUtils;

/**
 * Class for behavior of the "medical" Companion device profile role.
 */
public class CompanionDeviceMedicalRoleBehavior implements RoleBehavior {

    @Override
    public void grantAsUser(@NonNull Role role, @NonNull String packageName, boolean overrideUser,
            @NonNull UserHandle user, @NonNull Context context) {
        if (!UserUtils.isManagedProfile(user, context)) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.setNotificationPolicyAccessGranted(packageName, true);
            }
        }
    }

    @Override
    public void revokeAsUser(@NonNull Role role, @NonNull String packageName,
            @NonNull UserHandle user, @NonNull Context context) {
        if (!UserUtils.isManagedProfile(user, context)) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.setNotificationPolicyAccessGranted(packageName, false);
            }
        }
    }
}
