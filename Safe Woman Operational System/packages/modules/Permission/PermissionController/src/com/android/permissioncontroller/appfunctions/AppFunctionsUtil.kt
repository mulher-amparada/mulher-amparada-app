/*
 * Copyright 2025 The Android Open Source Project
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

package com.android.permissioncontroller.appfunctions

import android.app.appfunctions.AppFunctionManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.permission.flags.Flags
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import com.android.modules.utils.build.SdkLevel
import com.android.permissioncontroller.flags.Flags.automotivePrivacyDashboardAgentActivityEnabled
import com.android.permissioncontroller.flags.Flags.privacyDashboardAgentActivityEnabled
import com.android.permissioncontroller.permission.utils.Utils
import java.util.UUID

object AppFunctionsUtil {
    const val ACTION_MANAGE_PERMISSIONS =
        "com.android.permissioncontroller.devicestate.action.MANAGE_PERMISSIONS"

    const val ACTION_MANAGE_PERMISSION_APPS =
        "com.android.permissioncontroller.devicestate.action.MANAGE_PERMISSION_APPS"

    const val ACTION_MANAGE_APP_PERMISSIONS =
        "com.android.permissioncontroller.devicestate.action.MANAGE_APP_PERMISSIONS"

    const val ACTION_MANAGE_APP_PERMISSION =
        "com.android.permissioncontroller.devicestate.action.MANAGE_APP_PERMISSION"

    const val ACTION_MANAGE_UNUSED_APPS =
        "com.android.permissioncontroller.devicestate.action.MANAGE_UNUSED_APPS"

    const val ACTION_ADDITIONAL_PERMISSIONS =
        "com.android.permissioncontroller.devicestate.action.ADDITIONAL_PERMISSIONS"

    const val ACTION_MANAGE_DEFAULT_APP =
        "com.android.permissioncontroller.devicestate.action.MANAGE_DEFAULT_APP"

    const val LOG_TAG = "AppFunctionsUtil"
    const val EXTRA_DEVICE_STATE_KEY = "com.android.permissioncontroller.devicestate.key"
    const val DEVICE_STATE_PASSWORD_KEY = "device_state_password"
    private val sPasswordLock: Any = Any()

    @JvmStatic
    fun isIntentValid(intent: Intent, context: Context): Boolean {
        val passwordFromIntent = intent.getStringExtra(EXTRA_DEVICE_STATE_KEY) ?: return false
        val password = getPasswordForIntent(context)
        val valid = passwordFromIntent == password
        if (!valid) {
            Log.w(LOG_TAG, "Invalid password: $passwordFromIntent")
        }
        return valid
    }

    fun getPasswordForIntent(context: Context): String {
        synchronized(sPasswordLock) {
            val sharedPreferences = Utils.getDeviceProtectedSharedPreferences(context)
            var password = sharedPreferences.getString(DEVICE_STATE_PASSWORD_KEY, null)
            if (password == null) {
                password = UUID.randomUUID().toString()
                sharedPreferences.edit().putString(DEVICE_STATE_PASSWORD_KEY, password).apply()
            }
            return password
        }
    }

    @JvmStatic
    fun isValidAgent(packageName: String, context: Context): Boolean =
        Flags.appFunctionAccessApiEnabled() &&
            packageName in context.getSystemService(AppFunctionManager::class.java).validAgents

    @JvmStatic
    fun isValidTarget(packageName: String, context: Context): Boolean =
        Flags.appFunctionAccessApiEnabled() &&
            packageName in context.getSystemService(AppFunctionManager::class.java).validTargets

    @JvmStatic
    fun isAppFunctionUiEnabled(context: Context): Boolean {
        val packageManager = context.packageManager
        return Flags.appFunctionAccessUiEnabled() &&
            !packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) &&
            !packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE) &&
            !packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
    }

    @JvmStatic
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.CINNAMON_BUN)
    fun isPrivacyDashboardAgentActivityEnabled(context: Context): Boolean {
        val packageManager = context.packageManager
        return SdkLevel.isAtLeastB() &&
            android.app.appfunctions.flags.Flags.enableAppInteractionApi() &&
            privacyDashboardAgentActivityEnabled() &&
            (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE) ||
                automotivePrivacyDashboardAgentActivityEnabled()) &&
            !packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) &&
            !packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
    }
}
