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

package com.android.permissioncontroller.role.ui.behavior.v36;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.preference.Preference;

import com.android.permissioncontroller.permission.utils.Utils;
import com.android.permissioncontroller.role.ui.RequestRoleItemView;
import com.android.permissioncontroller.role.ui.TwoTargetPreference;
import com.android.permissioncontroller.role.ui.behavior.RoleUiBehavior;
import com.android.role.controller.model.Role;
import com.android.role.controller.util.UserUtils;

import java.util.List;

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
public class ReservedForTestingProfileGroupExclusivityRoleUiBehavior implements RoleUiBehavior {
    @Override
    public void preparePreferenceAsUser(@NonNull Role role, @NonNull TwoTargetPreference preference,
            @NonNull List<ApplicationInfo> applicationInfos, @NonNull UserHandle user,
            @NonNull Context context) {
        if (!applicationInfos.isEmpty()) {
            preparePreferenceInternal(preference.asPreference(), applicationInfos.get(0), false,
                    user, context);
        }
    }

    @Override
    public void prepareApplicationPreferenceAsUser(@NonNull Role role,
            @NonNull Preference preference, @NonNull ApplicationInfo applicationInfo,
            @NonNull UserHandle user, @NonNull Context context) {
        preparePreferenceInternal(preference, applicationInfo, true, user, context);
    }

    @Override
    public void prepareRequestRoleItemViewAsUser(@NonNull Role role,
            @NonNull RequestRoleItemView itemView, @NonNull ApplicationInfo applicationInfo,
            @NonNull UserHandle user, @NonNull Context context) {
        Context userContext = UserUtils.getUserContext(context, user);
        String title = getTitle(applicationInfo, userContext);
        itemView.getTitleTextView().setText(title);
    }

    private void preparePreferenceInternal(@NonNull Preference preference,
            @NonNull ApplicationInfo applicationInfo, boolean setTitle, @NonNull UserHandle user,
            @NonNull Context context) {
        Context userContext = UserUtils.getUserContext(context, user);
        String title = getTitle(applicationInfo, userContext);
        if (setTitle) {
            preference.setTitle(title);
        } else {
            preference.setSummary(title);
        }
    }

    @NonNull
    private static String getTitle(@NonNull ApplicationInfo applicationInfo,
            @NonNull Context context) {
        return Utils.getFullAppLabel(applicationInfo, context) + "@"
                + UserHandle.getUserHandleForUid(applicationInfo.uid).getIdentifier();
    }
}
