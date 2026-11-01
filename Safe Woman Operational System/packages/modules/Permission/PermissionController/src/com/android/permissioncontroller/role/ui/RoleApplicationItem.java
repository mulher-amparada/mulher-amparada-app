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

import android.content.pm.ApplicationInfo;

import androidx.annotation.NonNull;

/**
 * Information about an application to be displayed in a list of applications qualifying for a role.
 */
public class RoleApplicationItem {

    /**
     * The {@link ApplicationInfo} for this application.
     */
    @NonNull
    private final ApplicationInfo mApplicationInfo;

    /**
     * Whether this application is holding the role.
     */
    private final boolean mIsHolderApplication;

    /**
     * Whether access to screen and app context as is enabled. Only relevant for
     * {@link android.app.role.RoleManager#ROLE_ASSISTANT}. Value should map to
     * {@link android.app.AppOpsManager#OPSTR_READ_SCREEN_CONTEXT}. {@code true} when
     * {@link android.app.AppOpsManager#MODE_ALLOWED} or
     * {@link android.app.AppOpsManager#MODE_DEFAULT}, else {@code false}
     */
    private final boolean mReadScreenContextEnabled;

    public RoleApplicationItem(@NonNull ApplicationInfo applicationInfo,
            boolean isHolderApplication) {
        this(applicationInfo, isHolderApplication, false);
    }

    public RoleApplicationItem(@NonNull ApplicationInfo applicationInfo,
            boolean isHolderApplication, boolean isReadScreenContextEnabled) {
        mApplicationInfo = applicationInfo;
        mIsHolderApplication = isHolderApplication;
        mReadScreenContextEnabled = isReadScreenContextEnabled;
    }

    @NonNull
    public ApplicationInfo getApplicationInfo() {
        return mApplicationInfo;
    }

    public boolean isHolderApplication() {
        return mIsHolderApplication;
    }

    /**
     * Whether access to screen and app context as is enabled. Only relevant for
     * {@link android.app.role.RoleManager#ROLE_ASSISTANT}. Value should map to
     * {@link android.app.AppOpsManager#OPSTR_READ_SCREEN_CONTEXT}. {@code true} when
     * {@link android.app.AppOpsManager#MODE_ALLOWED} or
     * {@link android.app.AppOpsManager#MODE_DEFAULT}, else {@code false}
     */
    public boolean isReadScreenContextEnabled() {
        return mReadScreenContextEnabled;
    }
}
