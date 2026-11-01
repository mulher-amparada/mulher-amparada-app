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
package com.android.permissioncontroller.appfunctions.ui.handheld.v37

import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import androidx.preference.PreferenceFragmentCompat
import com.android.permissioncontroller.Constants.EXTRA_SESSION_ID
import com.android.permissioncontroller.permission.ui.ManagePermissionsActivity.EXTRA_SHOW_7_DAYS
import com.android.permissioncontroller.permission.ui.ManagePermissionsActivity.EXTRA_SHOW_SYSTEM
import com.android.permissioncontroller.permission.ui.handheld.PermissionsCollapsingToolbarBaseFragment

class AgentUsageDetailsWrapperFragment : PermissionsCollapsingToolbarBaseFragment() {
    override fun createPreferenceFragment(): PreferenceFragmentCompat = AgentUsageDetailsFragment()

    companion object {
        fun newInstance(
            sessionId: Long,
            packageName: String,
            user: UserHandle,
            showSystem: Boolean,
            show7Days: Boolean,
        ): AgentUsageDetailsWrapperFragment =
            AgentUsageDetailsWrapperFragment().apply {
                arguments =
                    Bundle().apply {
                        putLong(EXTRA_SESSION_ID, sessionId)
                        putString(Intent.EXTRA_PACKAGE_NAME, packageName)
                        putParcelable(Intent.EXTRA_USER, user)
                        putBoolean(EXTRA_SHOW_SYSTEM, showSystem)
                        putBoolean(EXTRA_SHOW_7_DAYS, show7Days)
                    }
            }
    }
}
