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

import androidx.preference.PreferenceFragmentCompat
import com.android.permissioncontroller.common.ui.handheld.SettingsFragment

/** Fragment for the app function agent list. */
class HandheldAgentListFragment : SettingsFragment(), HandheldAgentListPreferenceFragment.Parent {
    override fun onCreatePreferenceFragment(): PreferenceFragmentCompat {
        return HandheldAgentListPreferenceFragment.newInstance()
    }

    companion object {
        /**
         * Create a new instance of this fragment.
         *
         * @return a new instance of this fragment
         */
        @JvmStatic
        fun newInstance(): HandheldAgentListFragment {
            return HandheldAgentListFragment()
        }
    }
}
