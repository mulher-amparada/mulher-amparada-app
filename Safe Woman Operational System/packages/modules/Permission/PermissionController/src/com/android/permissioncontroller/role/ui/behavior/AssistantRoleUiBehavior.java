/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.permissioncontroller.role.ui.behavior;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.permission.flags.Flags;
import com.android.permissioncontroller.R;
import com.android.permissioncontroller.permission.utils.Utils;
import com.android.permissioncontroller.role.ui.RoleApplicationItem;
import com.android.permissioncontroller.role.utils.PackageUtils;
import com.android.role.controller.model.Role;
import com.android.role.controller.util.SignedPackage;
import com.android.role.controller.util.SignedPackageUtils;
import com.android.role.controller.util.UserUtils;

import java.util.List;
import java.util.function.Predicate;

/***
 * Class for UI behavior of Assistant role
 */
public class AssistantRoleUiBehavior implements RoleUiBehavior {

    @Nullable
    @Override
    public Intent getManageIntentAsUser(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean isAutomotive = packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE);
        if (isAutomotive) {
            return null;
        }
        if (android.permission.flags.Flags.assistSettingsPrivacyImprovementsEnabled()
                && !packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                && !packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            return null;
        }
        return new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS);
    }

    @Nullable
    @Override
    public String getRecommendedApplicationsTitle(@NonNull Role role, @NonNull Context context) {
        if (Flags.defaultAppsRecommendationEnabled()) {
            String by = context.getString(R.string.config_recommendedAssistantsBy);
            if (!by.isEmpty()) {
                return context.getString(R.string.role_assistant_recommended_title, by);
            }
        }
        return RoleUiBehavior.super.getRecommendedApplicationsTitle(role, context);
    }

    @Nullable
    @Override
    public String getRecommendedApplicationsDescription(@NonNull Role role,
            @NonNull Context context) {
        if (Flags.defaultAppsRecommendationEnabled()) {
            String description =
                    context.getString(R.string.config_recommendedAssistantsDescription);
            if (!description.isEmpty()) {
                return description;
            }
        }
        return RoleUiBehavior.super.getRecommendedApplicationsDescription(role, context);
    }

    @NonNull
    @Override
    public Predicate<RoleApplicationItem> getRecommendedApplicationFilter(
            @NonNull Role role, @NonNull Context context) {
        if (Flags.defaultAppsRecommendationEnabled()
                && getRecommendedApplicationsTitle(role, context) != null
                && getRecommendedApplicationsDescription(role, context) != null) {
            List<SignedPackage> signedPackages = SignedPackage.parseList(
                    context.getResources().getString(R.string.config_recommendedAssistants));
            return applicationItem -> SignedPackageUtils.matchesAny(
                    applicationItem.getApplicationInfo(), signedPackages, context);
        }
        return RoleUiBehavior.super.getRecommendedApplicationFilter(role, context);
    }

    @Nullable
    @Override
    public ConfirmationDialogInfo getConfirmationDialogInfo(@NonNull Role role,
            @NonNull String packageName, @NonNull UserHandle user, @NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA
                && android.permission.flags.Flags.newDefaultAppConfirmationDialogEnabled()
                && android.permission.flags.Flags.assistSettingsPrivacyImprovementsEnabled()) {
            ApplicationInfo applicationInfo =
                    PackageUtils.getApplicationInfoAsUser(packageName, user, context);
            String appLabel = applicationInfo != null ? Utils.getFullAppLabel(applicationInfo,
                    context) : packageName;
            String escapedAppLabel = Html.escapeHtml(appLabel);
            CharSequence message;
            if (isReadScreenContextAllowed(packageName, user, context)) {
                message = Html.fromHtml(context.getString(
                        R.string.assistant_confirmation_message_access_restored_v37,
                        escapedAppLabel));
            } else {
                message = context.getString(R.string.assistant_confirmation_message_v37);
            }
            return new ConfirmationDialogInfo(true,
                    Html.fromHtml(context.getString(R.string.assistant_confirmation_title_v37,
                            escapedAppLabel)),
                    message,
                    context.getString(R.string.default_app_confirmation_positive_button),
                    context.getString(R.string.default_app_confirmation_negative_button), true);
        } else {
            return new ConfirmationDialogInfo(null,
                    context.getString(R.string.assistant_confirmation_message),
                    context.getString(android.R.string.ok),
                    context.getString(android.R.string.cancel), true);
        }
    }

    private static boolean isReadScreenContextAllowed(@NonNull String packageName,
            @NonNull UserHandle user, @NonNull Context context) {
        Context userContext = UserUtils.getUserContext(context, user);
        PackageManager userPackageManager = userContext.getPackageManager();
        int uid;
        try {
            uid = userPackageManager.getPackageUid(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        AppOpsManager appOpsManager = userContext.getSystemService(AppOpsManager.class);
        return appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_READ_SCREEN_CONTEXT, uid,
                packageName) == AppOpsManager.MODE_ALLOWED;
    }
}
