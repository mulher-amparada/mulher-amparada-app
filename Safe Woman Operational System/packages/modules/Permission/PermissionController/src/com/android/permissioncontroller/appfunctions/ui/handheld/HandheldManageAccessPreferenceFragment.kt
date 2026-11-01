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

import android.os.Bundle
import android.view.View
import com.android.permissioncontroller.appfunctions.ui.ManageAccessActivity
import com.android.permissioncontroller.appfunctions.ui.ManageAccessChildFragment
import com.android.settingslib.widget.SettingsBasePreferenceFragment

/**
 * Handheld preference fragment for the management of app function agent access of targets.
 *
 * Must be added as a child fragment and its parent fragment must implement [Parent].
 */
class HandheldManageAccessPreferenceFragment :
    SettingsBasePreferenceFragment(), ManageAccessChildFragment.Parent {
    private lateinit var agentPackageName: String
    private lateinit var targetPackageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = arguments!!
        agentPackageName = arguments.getString(ManageAccessActivity.EXTRA_AGENT_PACKAGE_NAME)!!
        targetPackageName = arguments.getString(ManageAccessActivity.EXTRA_TARGET_PACKAGE_NAME)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            val fragment =
                ManageAccessChildFragment.newInstance(agentPackageName, targetPackageName)
            childFragmentManager.beginTransaction().add(fragment, null).commit()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // R.xml.manage_app_function_access will be added by the child fragment.
    }

    override fun setTitle(title: CharSequence) {
        requireParent().setTitle(title)
    }

    override fun onPreferenceScreenChanged() {
        requireParent().onPreferenceScreenChanged()
    }

    private fun requireParent(): Parent {
        return requireParentFragment() as Parent
    }

    /** Interface that the parent fragment must implement. */
    interface Parent {
        /**
         * Set the title of the current settings page.
         *
         * @param title the title of the current settings page
         */
        fun setTitle(title: CharSequence)

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
         * @param targetPackageName target package to modify access for
         * @return a new instance of this fragment
         */
        @JvmStatic
        fun newInstance(
            agentPackageName: String,
            targetPackageName: String,
        ): HandheldManageAccessPreferenceFragment {
            val arguments =
                Bundle().apply {
                    putString(ManageAccessActivity.EXTRA_AGENT_PACKAGE_NAME, agentPackageName)
                    putString(ManageAccessActivity.EXTRA_TARGET_PACKAGE_NAME, targetPackageName)
                }
            return HandheldManageAccessPreferenceFragment().apply { setArguments(arguments) }
        }
    }
}
