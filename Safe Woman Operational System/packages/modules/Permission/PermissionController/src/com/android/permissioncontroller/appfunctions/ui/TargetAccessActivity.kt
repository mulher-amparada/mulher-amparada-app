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
package com.android.permissioncontroller.appfunctions.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil
import com.android.permissioncontroller.appfunctions.ui.handheld.HandheldTargetAccessFragment
import com.android.permissioncontroller.common.ui.SettingsActivity

/** Activity to manage app function target access. */
class TargetAccessActivity : SettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AppFunctionsUtil.isAppFunctionUiEnabled(this)) {
            Log.w(
                LOG_TAG,
                "App Function isn't enabled: Either the platform is not supported " +
                    "or the UI flag FLAG_APP_FUNCTION_ACCESS_UI_ENABLED isn't enabled.",
            )
            finish()
            return
        }

        val targetPackageName: String? = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)
        if (
            targetPackageName.isNullOrEmpty() ||
                !AppFunctionsUtil.isValidTarget(targetPackageName, this)
        ) {
            Log.e(LOG_TAG, "Unknown/Invalid package: $targetPackageName")
            finish()
            return
        }

        if (savedInstanceState == null) {
            val fragment = HandheldTargetAccessFragment.newInstance(targetPackageName)
            supportFragmentManager.beginTransaction().add(android.R.id.content, fragment).commit()
        }
    }

    companion object {
        private val LOG_TAG = TargetAccessActivity::class.java.simpleName
    }
}
