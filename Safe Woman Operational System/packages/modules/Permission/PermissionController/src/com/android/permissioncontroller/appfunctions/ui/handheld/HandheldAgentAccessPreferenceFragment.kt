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
import android.view.View
import androidx.preference.Preference
import com.android.permissioncontroller.appfunctions.ui.AgentAccessChildFragment
import com.android.settingslib.widget.IntroPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment

/**
 * Handheld preference fragment for the management of app function agents.
 *
 * Must be added as a child fragment and its parent fragment must implement [Parent].
 */
class HandheldAgentAccessPreferenceFragment :
    SettingsBasePreferenceFragment(), AgentAccessChildFragment.Parent {
    private lateinit var agentPackageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        agentPackageName = arguments!!.getString(Intent.EXTRA_PACKAGE_NAME)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            val fragment = AgentAccessChildFragment.newInstance(agentPackageName)
            childFragmentManager.beginTransaction().add(fragment, null).commit()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Preferences will be added by the child fragment later.
    }

    override fun createHeaderPreference(): Preference = IntroPreference(requireContext())

    override fun createPreference(): Preference =
        Preference(requireContext()).apply { isPersistent = false }

    override fun onPreferenceScreenChanged() {
        requireParent().onPreferenceScreenChanged()
    }

    private fun requireParent(): Parent {
        return requireParentFragment() as Parent
    }

    /** Interface that the parent fragment must implement. */
    interface Parent {
        /**
         * Callback when changes have been made to the {@link androidx.preference.PreferenceScreen}
         * of this {@link PreferenceFragmentCompat}.
         */
        fun onPreferenceScreenChanged()
    }

    companion object {
        /**
         * Create a new instance of this fragment.
         *
         * @param agentPackageName agent package to modify access for
         * @return a new instance of this fragment
         */
        @JvmStatic
        fun newInstance(agentPackageName: String): HandheldAgentAccessPreferenceFragment {
            val arguments =
                Bundle().apply { putString(Intent.EXTRA_PACKAGE_NAME, agentPackageName) }
            return HandheldAgentAccessPreferenceFragment().apply { setArguments(arguments) }
        }
    }
}
