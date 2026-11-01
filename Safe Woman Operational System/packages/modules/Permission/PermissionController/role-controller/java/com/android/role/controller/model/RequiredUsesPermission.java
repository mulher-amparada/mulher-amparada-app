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

package com.android.role.controller.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.role.controller.util.ArrayUtils;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A uses-permission required by a {@link Role}.
 */
public class RequiredUsesPermission extends Requirement {

    /**
     * The name of this required uses-permission.
     */
    @NonNull
    private final String mName;

    public RequiredUsesPermission(@NonNull String name, @Nullable Supplier<Boolean> featureFlag,
            int minTargetSdkVersion) {
        super(featureFlag, minTargetSdkVersion);

        mName = name;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @Override
    public boolean isQualifiedAsUser(@NonNull PackageInfo packageInfo, @NonNull UserHandle user,
            @NonNull Context context) {
        return ArrayUtils.contains(packageInfo.requestedPermissions, mName);
    }

    @Override
    public String toString() {
        return "RequiredUsesPermission{"
                + "mName='" + mName + '\''
                + "} " + super.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        RequiredUsesPermission that = (RequiredUsesPermission) object;
        return Objects.equals(mName, that.mName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mName);
    }
}
