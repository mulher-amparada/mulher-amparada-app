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

package com.android.permissioncontroller.role;

import android.content.pm.ApplicationInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.core.os.ParcelCompat;

import java.util.Objects;

/**
 *  Data class storing a {@link UserHandle} and a package name.
 */
public class UserPackage implements Parcelable {
    @NonNull
    public final UserHandle user;
    @NonNull
    public final String packageName;

    private UserPackage(@NonNull UserHandle user, @NonNull String packageName) {
        this.user = user;
        this.packageName = packageName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UserPackage) {
            UserPackage other = (UserPackage) obj;
            return Objects.equals(user, other.user)
                    && Objects.equals(packageName, other.packageName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, packageName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
        out.writeParcelable(user, 0);
        out.writeString(packageName);
    }

    public static final Parcelable.Creator<UserPackage> CREATOR =
            new Parcelable.Creator<>() {
                @Override
                public UserPackage createFromParcel(Parcel in) {
                    UserHandle user =
                            ParcelCompat.readParcelable(
                                    in, UserHandle.class.getClassLoader(), UserHandle.class);
                    String packageName = in.readString();
                    if (user == null || packageName == null) {
                        return null;
                    }
                    return UserPackage.of(user, packageName);
                }

                @Override
                public UserPackage[] newArray(int size) {
                    return new UserPackage[size];
                }
            };

    /** Returns a {@link UserPackage} represented by the specified user and package name */
    @NonNull
    public static UserPackage of(@NonNull UserHandle user, @NonNull String packageName) {
        return new UserPackage(user, packageName);
    }

    /** Returns a {@link UserPackage} using user and package name from an {@link ApplicationInfo} */
    @NonNull
    public static UserPackage from(@NonNull ApplicationInfo applicationInfo) {
        return new UserPackage(
                UserHandle.getUserHandleForUid(applicationInfo.uid), applicationInfo.packageName);
    }
}
