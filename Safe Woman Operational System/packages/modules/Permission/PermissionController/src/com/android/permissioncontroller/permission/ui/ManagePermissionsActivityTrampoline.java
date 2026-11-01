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

package com.android.permissioncontroller.permission.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.android.permissioncontroller.Constants;
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil;
import com.android.permissioncontroller.permission.service.PermissionSearchIndexablesProvider;

/**
 * Trampoline activity for {@link ManagePermissionsActivity}.
 */
public class ManagePermissionsActivityTrampoline extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (!PermissionSearchIndexablesProvider.isIntentValid(intent, this)
                && !AppFunctionsUtil.isIntentValid(intent, this)) {
            finish();
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            finish();
            return;
        }

        Intent newIntent = new Intent(this, ManagePermissionsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        switch (action) {
            case PermissionSearchIndexablesProvider.ACTION_MANAGE_PERMISSION_APPS:
                newIntent.setAction(Intent.ACTION_MANAGE_PERMISSION_APPS).putExtra(
                        Intent.EXTRA_PERMISSION_GROUP_NAME,
                        PermissionSearchIndexablesProvider.getOriginalKey(intent));
                break;
            case AppFunctionsUtil.ACTION_MANAGE_PERMISSIONS:
                newIntent.setAction(Intent.ACTION_MANAGE_PERMISSIONS);
                break;
            case AppFunctionsUtil.ACTION_MANAGE_PERMISSION_APPS:
                newIntent.setAction(Intent.ACTION_MANAGE_PERMISSION_APPS).putExtra(
                        Intent.EXTRA_PERMISSION_GROUP_NAME,
                        intent.getStringExtra(Intent.EXTRA_PERMISSION_GROUP_NAME));
                break;
            case AppFunctionsUtil.ACTION_MANAGE_APP_PERMISSIONS:
                newIntent.setAction(Settings.ACTION_APP_PERMISSIONS_SETTINGS).putExtra(
                        Intent.EXTRA_PACKAGE_NAME,
                        intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME));
                break;
            case AppFunctionsUtil.ACTION_MANAGE_APP_PERMISSION:
                newIntent.setAction(Intent.ACTION_MANAGE_APP_PERMISSION).putExtra(
                                Intent.EXTRA_PERMISSION_GROUP_NAME,
                                intent.getStringExtra(Intent.EXTRA_PERMISSION_GROUP_NAME))
                        .putExtra(Intent.EXTRA_PACKAGE_NAME,
                                intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME))
                        .putExtra(Intent.EXTRA_USER, android.os.Process.myUserHandle());
                break;
            case AppFunctionsUtil.ACTION_MANAGE_UNUSED_APPS:
                newIntent.setAction(Intent.ACTION_MANAGE_UNUSED_APPS);
                break;
            case AppFunctionsUtil.ACTION_ADDITIONAL_PERMISSIONS:
                newIntent.setAction(Constants.ACTION_ADDITIONAL_PERMISSIONS);
                break;
            default:
                finish();
                return;
        }

        startActivity(newIntent);
        finish();
    }
}
