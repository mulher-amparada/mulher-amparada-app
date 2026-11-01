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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Specifies a requirement for an application to qualify for a {@link Role}.
 */
public abstract class Requirement {

    /**
     * The feature flag for this requirement to be required, or {@code null} if none.
     */
    @Nullable
    private final Supplier<Boolean> mFeatureFlag;

    /**
     * The minimum target SDK version for this requirement to be required.
     * <p>
     * This also implies a minimum platform SDK version for this requirement to be required.
     */
    private final int mMinTargetSdkVersion;

    public Requirement(@Nullable Supplier<Boolean> featureFlag, int minTargetSdkVersion) {
        mFeatureFlag = featureFlag;
        mMinTargetSdkVersion = minTargetSdkVersion;
    }

    @Nullable
    public Supplier<Boolean> getFeatureFlag() {
        return mFeatureFlag;
    }

    public int getMinTargetSdkVersion() {
        return mMinTargetSdkVersion;
    }

    /**
     * Check whether this requirement is available.
     *
     * @return whether this requirement is available
     */
    public boolean isAvailable() {
        if (mFeatureFlag != null && !mFeatureFlag.get()) {
            return false;
        }
        if (Build.VERSION.SDK_INT < mMinTargetSdkVersion) {
            return false;
        }
        return true;
    }

    /**
     * Check whether this requirement is required for a package.
     *
     * @param applicationInfo the {@link ApplicationInfo} for the package
     *
     * @return whether this requirement is required
     */
    public boolean isRequired(@NonNull ApplicationInfo applicationInfo) {
        return isAvailable() && applicationInfo.targetSdkVersion >= mMinTargetSdkVersion;
    }

    /**
     * Check whether a package is qualified for this requirement.
     *
     * @param packageInfo the {@link PackageInfo} for the package
     * @param user the {@code UserHandle} to check the requirement for
     * @param context the {@code Context} to check the requirement for
     *
     * @return whether the package is qualified for this requirement
     */
    public abstract boolean isQualifiedAsUser(@NonNull PackageInfo packageInfo,
            @NonNull UserHandle user, @NonNull Context context);

    @Override
    public String toString() {
        return "Requirement{"
                + "mFeatureFlag=" + mFeatureFlag
                + ", mMinTargetSdkVersion=" + mMinTargetSdkVersion
                + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Requirement that = (Requirement) object;
        return mMinTargetSdkVersion == that.mMinTargetSdkVersion
                && Objects.equals(mFeatureFlag, that.mFeatureFlag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mFeatureFlag, mMinTargetSdkVersion);
    }
}
