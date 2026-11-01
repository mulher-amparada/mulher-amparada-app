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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil
import com.android.permissioncontroller.appfunctions.ui.handheld.HandheldAgentAccessFragment
import com.android.permissioncontroller.common.ui.SettingsActivity

/** Activity to manage app function agent access. */
class AgentAccessActivity : SettingsActivity() {
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

        val agentPackageName: String? = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)
        if (
            agentPackageName.isNullOrEmpty() ||
                !AppFunctionsUtil.isValidAgent(agentPackageName, this)
        ) {
            Log.e(LOG_TAG, "Unknown/Invalid package: $agentPackageName")
            finish()
            return
        }

        if (savedInstanceState == null) {
            val fragment = HandheldAgentAccessFragment.newInstance(agentPackageName)
            supportFragmentManager.beginTransaction().add(android.R.id.content, fragment).commit()
        }
    }

    companion object {
        private val LOG_TAG = AgentAccessActivity::class.java.simpleName

        /**
         * Create an intent for starting this activity.
         *
         * @param context the context to create the intent
         * @param agentPackageName the package whose access will be managed by the activity
         * @return an intent to start this activity
         */
        fun createIntent(context: Context, agentPackageName: String) =
            Intent(context, AgentAccessActivity::class.java)
                .putExtra(Intent.EXTRA_PACKAGE_NAME, agentPackageName)
    }
}
