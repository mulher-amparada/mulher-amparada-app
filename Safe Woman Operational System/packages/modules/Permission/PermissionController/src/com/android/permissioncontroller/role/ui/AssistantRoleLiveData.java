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

package com.android.permissioncontroller.role.ui;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import com.android.role.controller.model.Role;

import java.util.Objects;

/**
 * A {@link RoleLiveData} that fills in the {@link RoleApplicationItem} isReadScreenContextEnabled
 * value using {@link AppOpsManager#OPSTR_READ_SCREEN_CONTEXT} mode.
 */
public class AssistantRoleLiveData extends RoleLiveData
        implements AppOpsManager.OnOpChangedListener {

    @NonNull
    private final AppOpsManager mAppOpsManager;

    public AssistantRoleLiveData(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Context context) {
        super(role, user, context);
        mAppOpsManager = context.getSystemService(AppOpsManager.class);
    }

    @Override
    protected void onActive() {
        super.onActive();
        mAppOpsManager.startWatchingMode(AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
                null, this);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        mAppOpsManager.stopWatchingMode(this);
    }

    @NonNull
    @Override
    protected RoleApplicationItem createRoleApplicationItem(
            @NonNull ApplicationInfo applicationInfo, boolean isHolderApplication) {
        int appOpMode = mAppOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_READ_SCREEN_CONTEXT, applicationInfo.uid,
                applicationInfo.packageName);
        boolean isReadScreenContextEnabled = appOpMode == AppOpsManager.MODE_ALLOWED
                || appOpMode == AppOpsManager.MODE_DEFAULT;
        return new RoleApplicationItem(applicationInfo, isHolderApplication,
                isReadScreenContextEnabled);
    }

    @Override
    public void onOpChanged(String op, String packageName) {
        if (Objects.equals(AppOpsManager.OPSTR_READ_SCREEN_CONTEXT, op)) {
            loadValue();
        }
    }
}
