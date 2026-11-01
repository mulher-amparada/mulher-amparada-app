/*
 * Copyright (C) 2026 The Android Open Source Project
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
package com.android.permissioncontroller.appfunctions.ui.v37

import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import android.util.Log
import com.android.permissioncontroller.DeviceUtils
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil
import com.android.permissioncontroller.appfunctions.ui.handheld.v37.AgentUsageDetailsWrapperFragment
import com.android.permissioncontroller.common.ui.SettingsActivity
import com.android.permissioncontroller.permission.ui.ManagePermissionsActivity.EXTRA_SHOW_7_DAYS
import com.android.permissioncontroller.permission.ui.ManagePermissionsActivity.EXTRA_SHOW_SYSTEM
import com.android.permissioncontroller.permission.ui.auto.dashboard.AutoAgentUsageDetailsFragment
import kotlin.random.Random

/** Activity for reviewing the timeline history of the agent */
class AgentUsageDetailsActivity : SettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AppFunctionsUtil.isPrivacyDashboardAgentActivityEnabled(this)) {
            Log.w(
                LOG_TAG,
                "The feature isn't enabled or the form factor isn't" +
                    "supported. Finishing the activity.",
            )
            finish()
            return
        }

        if (savedInstanceState == null) {
            val agentPackageName = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)!!
            val user = intent.getParcelableExtra(Intent.EXTRA_USER, UserHandle::class.java)!!
            val showSystem = intent.getBooleanExtra(EXTRA_SHOW_SYSTEM, false)
            val show7Days = intent.getBooleanExtra(EXTRA_SHOW_7_DAYS, false)
            val sessionId = Random.nextLong()
            val fragment =
                if (DeviceUtils.isAuto(this)) {
                    AutoAgentUsageDetailsFragment.newInstance(agentPackageName, user)
                } else {
                    AgentUsageDetailsWrapperFragment.newInstance(
                        sessionId,
                        agentPackageName,
                        user,
                        showSystem,
                        show7Days,
                    )
                }
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
        }
    }

    companion object {
        private val LOG_TAG = AgentUsageDetailsActivity::class.java.simpleName
    }
}
