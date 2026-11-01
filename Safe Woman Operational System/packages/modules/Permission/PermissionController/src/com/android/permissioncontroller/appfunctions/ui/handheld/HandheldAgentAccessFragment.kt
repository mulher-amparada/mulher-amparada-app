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
package com.android.permissioncontroller.appfunctions.ui.handheld

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.android.permissioncontroller.common.ui.handheld.SettingsFragment

/** Fragment for the app function agent list. */
class HandheldAgentAccessFragment :
    SettingsFragment(), HandheldAgentAccessPreferenceFragment.Parent {
    private lateinit var agentPackageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        agentPackageName = arguments!!.getString(Intent.EXTRA_PACKAGE_NAME)!!
    }

    override fun onCreatePreferenceFragment(): PreferenceFragmentCompat {
        return HandheldAgentAccessPreferenceFragment.newInstance(agentPackageName)
    }

    companion object {
        /**
         * Create a new instance of this fragment.
         *
         * @param agentPackageName agent package to modify access for
         * @return a new instance of this fragment
         */
        @JvmStatic
        fun newInstance(agentPackageName: String): HandheldAgentAccessFragment {
            val arguments =
                Bundle().apply { putString(Intent.EXTRA_PACKAGE_NAME, agentPackageName) }
            return HandheldAgentAccessFragment().apply { setArguments(arguments) }
        }
    }
}
