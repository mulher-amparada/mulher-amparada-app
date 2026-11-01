/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static com.android.permissioncontroller.PermissionControllerStatsLog.ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED;
import static com.android.permissioncontroller.PermissionControllerStatsLog.ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED__RESULT__RESULT_CANCELLED;
import static com.android.permissioncontroller.PermissionControllerStatsLog.ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED__RESULT__RESULT_OK;
import static com.android.permissioncontroller.PermissionControllerStatsLog.ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED__RESULT__RESULT_UNSPECIFIED;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.android.permissioncontroller.PermissionControllerStatsLog;
import com.android.permissioncontroller.R;
import com.android.permissioncontroller.permission.utils.Utils;
import com.android.permissioncontroller.role.ui.behavior.ConfirmationDialogInfo;
import com.android.permissioncontroller.role.utils.PackageUtils;
import com.android.settingslib.widget.SettingsThemeHelper;

/**
 * {@link DialogFragment} for confirmation before setting a default app.
 */
public class DefaultAppConfirmationDialogFragment extends DialogFragment {
    private static final String LOG_TAG =
            DefaultAppConfirmationDialogFragment.class.getSimpleName();
    private static final String EXTRA_INFO =
            DefaultAppConfirmationDialogFragment.class.getName() + ".extra.INFO";

    @NonNull
    private String mRoleName;
    @NonNull
    private String mPackageName;
    private int mUid;
    @NonNull
    private ConfirmationDialogInfo mInfo;

    /**
     * Create a new instance of this fragment.
     *
     * @param roleName the name of the role being changed
     * @param packageName the package name of the application
     * @param uid the UID the specified package is running in
     * @param info the info for this confirmation dialog
     *
     * @return a new instance of this fragment
     *
     * @see #show(String, String, int, ConfirmationDialogInfo, Fragment)
     */
    @NonNull
    public static DefaultAppConfirmationDialogFragment newInstance(@NonNull String roleName,
            @NonNull String packageName, int uid, @NonNull ConfirmationDialogInfo info) {
        DefaultAppConfirmationDialogFragment fragment = new DefaultAppConfirmationDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Intent.EXTRA_ROLE_NAME, roleName);
        arguments.putString(Intent.EXTRA_PACKAGE_NAME, packageName);
        arguments.putInt(Intent.EXTRA_UID, uid);
        arguments.putParcelable(EXTRA_INFO, info);
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Show a new instance of this fragment.
     *
     * @param roleName the name of the role being changed
     * @param packageName the package name of the application
     * @param uid the UID the specified package is running in
     * @param info the info for this confirmation dialog
     * @param fragment the parent fragment
     *
     * @see #newInstance(String, String, int, ConfirmationDialogInfo)
     */
    public static void show(@NonNull String roleName, @NonNull String packageName, int uid,
            @NonNull ConfirmationDialogInfo info, @NonNull Fragment fragment) {
        newInstance(roleName, packageName, uid, info)
                .show(fragment.getChildFragmentManager(), null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mRoleName = arguments.getString(Intent.EXTRA_ROLE_NAME);
        mPackageName = arguments.getString(Intent.EXTRA_PACKAGE_NAME);
        mUid = arguments.getInt(Intent.EXTRA_UID);
        mInfo = BundleCompat.getParcelable(arguments, EXTRA_INFO, ConfirmationDialogInfo.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        if (mInfo.isChangeConfirmation() && SettingsThemeHelper.isExpressiveTheme(context)) {
            setStyle(DialogFragment.STYLE_NORMAL,
                    R.style.Theme_DeviceDefault_AlertDialog_DefaultAppConfirmation_Expressive);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, getTheme())
                .setMessage(mInfo.getMessage())
                .setPositiveButton(mInfo.getPositiveButtonText(),
                        (dialog, which) -> onPositiveButtonClicked())
                .setNegativeButton(mInfo.getNegativeButtonText(),
                        (dialog, which) -> onNegativeButtonClicked());
        if (mInfo.shouldShowIcon()) {
            ApplicationInfo applicationInfo = PackageUtils.getApplicationInfoAsUser(mPackageName,
                    UserHandle.getUserHandleForUid(mUid), context);
            LayoutInflater inflater = LayoutInflater.from(builder.getContext());
            View titleLayout = inflater.inflate(R.layout.default_app_confirmation_dialog_title,
                    null);
            ImageView iconView = titleLayout.requireViewById(R.id.icon);
            if (applicationInfo != null) {
                iconView.setImageDrawable(Utils.getBadgedIcon(context, applicationInfo));
            } else {
                Log.w(LOG_TAG,
                        "Cannot get ApplicationInfo for application, package name: " + mPackageName
                                + ", user id: " + UserHandle.getUserHandleForUid(
                                mUid).getIdentifier());
            }
            TextView titleView = titleLayout.requireViewById(R.id.title);
            titleView.setText(mInfo.getTitle());
            builder.setCustomTitle(titleLayout);
        } else {
            builder.setTitle(mInfo.getTitle());
        }
        return builder.create();
    }

    private void onPositiveButtonClicked() {
        PermissionControllerStatsLog.write(ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED, mUid,
                mPackageName, mRoleName,
                ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED__RESULT__RESULT_OK);
        Listener listener = (Listener) getParentFragment();
        listener.setDefaultApp(mPackageName, mUid);
    }

    private void onNegativeButtonClicked() {
        PermissionControllerStatsLog.write(ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED, mUid,
                mPackageName, mRoleName,
                ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED__RESULT__RESULT_CANCELLED);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        PermissionControllerStatsLog.write(ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED, mUid,
                mPackageName, mRoleName,
                ROLE_SETTINGS_CONFIRMATION_DIALOG_ACTION_REPORTED__RESULT__RESULT_UNSPECIFIED);
    }

    /**
     * Listener for {@link DefaultAppConfirmationDialogFragment}.
     */
    public interface Listener {

        /**
         * Set an application as the default app.
         *
         * @param packageName the package name of the application
         */
        void setDefaultApp(@NonNull String packageName, int uid);
    }
}
