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

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil
import com.android.permissioncontroller.appfunctions.ui.handheld.HandheldManageAccessFragment
import com.android.permissioncontroller.common.ui.SettingsActivity

/** Activity to manage app function access. */
class ManageAccessActivity : SettingsActivity() {
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

        val agentPackageName = intent.getStringExtra(EXTRA_AGENT_PACKAGE_NAME)
        if (
            agentPackageName.isNullOrEmpty() ||
                !AppFunctionsUtil.isValidAgent(agentPackageName, this)
        ) {
            Log.e(LOG_TAG, "Unknown/Invalid package: $agentPackageName")
            finish()
            return
        }

        val targetPackageName = intent.getStringExtra(EXTRA_TARGET_PACKAGE_NAME)
        if (
            targetPackageName.isNullOrEmpty() ||
                !AppFunctionsUtil.isValidTarget(targetPackageName, this)
        ) {
            Log.e(LOG_TAG, "Unknown/Invalid package: $targetPackageName")
            finish()
            return
        }

        if (savedInstanceState == null) {
            val fragment =
                HandheldManageAccessFragment.newInstance(agentPackageName, targetPackageName)
            supportFragmentManager.beginTransaction().add(R.id.content, fragment).commit()
        }
    }

    companion object {
        private val LOG_TAG = ManageAccessActivity::class.java.simpleName
        const val EXTRA_AGENT_PACKAGE_NAME =
            "com.android.permissioncontroller.extra.AGENT_PACKAGE_NAME"
        const val EXTRA_TARGET_PACKAGE_NAME =
            "com.android.permissioncontroller.extra.TARGET_PACKAGE_NAME"

        /**
         * Create an intent for starting this activity.
         *
         * @param context the context to create the intent
         * @param agentPackageName the agent package whose access will be managed by the activity
         * @param targetPackageName the target package whose access will be managed by the activity
         * @return an intent to start this activity
         */
        fun createIntent(context: Context, agentPackageName: String, targetPackageName: String) =
            Intent(context, ManageAccessActivity::class.java)
                .putExtra(EXTRA_AGENT_PACKAGE_NAME, agentPackageName)
                .putExtra(EXTRA_TARGET_PACKAGE_NAME, targetPackageName)
    }
}
