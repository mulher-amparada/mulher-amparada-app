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

import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.android.permissioncontroller.R
import com.android.permissioncontroller.appfunctions.domain.model.v31.AppFunctionPackageInfo
import com.android.permissioncontroller.appfunctions.domain.usecase.GetAppFunctionPackageInfoUseCaseImpl
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAppFunctionPackageInfoUseCase
import com.android.permissioncontroller.appfunctions.ui.viewmodel.ManageAccessViewModel
import com.android.permissioncontroller.appfunctions.ui.viewmodel.ManageAccessViewModelFactory
import com.android.permissioncontroller.common.model.Stateful
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository
import com.android.settingslib.widget.SelectorWithWidgetPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Child fragment for modifying agent access of target apps.
 *
 * <p>
 * Must be added as a child fragment and its parent fragment must be a {@link
 * PreferenceFragmentCompat} that implements {@link Parent}.
 *
 * @param <PF> type of the parent fragment
 */
class ManageAccessChildFragment<PF> : Fragment()
    where PF : PreferenceFragmentCompat, PF : ManageAccessChildFragment.Parent {
    private lateinit var agentPackageName: String
    private lateinit var targetPackageName: String

    private lateinit var introPreference: Preference
    private lateinit var radioButtonCategory: PreferenceCategory
    private lateinit var allowRadioPreference: SelectorWithWidgetPreference
    private lateinit var denyRadioPreference: SelectorWithWidgetPreference

    private lateinit var viewModel: ManageAccessViewModel
    private lateinit var getAppFunctionPackageInfoUseCase: GetAppFunctionPackageInfoUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = arguments!!
        agentPackageName = arguments.getString(ManageAccessActivity.EXTRA_AGENT_PACKAGE_NAME)!!
        targetPackageName = arguments.getString(ManageAccessActivity.EXTRA_TARGET_PACKAGE_NAME)!!

        val factory =
            ManageAccessViewModelFactory(
                requireActivity().application,
                agentPackageName,
                targetPackageName,
            )
        viewModel = ViewModelProvider(this, factory).get(ManageAccessViewModel::class.java)
        val packageRepository = PackageRepository.createInstance(requireContext())
        getAppFunctionPackageInfoUseCase = GetAppFunctionPackageInfoUseCaseImpl(packageRepository)

        val preferenceFragment = requirePreferenceFragment()
        preferenceFragment.lifecycleScope.launch {
            preferenceFragment.lifecycle.repeatOnLifecycle(State.CREATED) {
                preferenceFragment.setPreferencesFromResource(
                    R.xml.manage_app_function_access,
                    null,
                )

                val preferenceScreen = requirePreferenceFragment().preferenceScreen!!
                introPreference = preferenceScreen.findPreference(PREFERENCE_KEY_INTRO)!!
                radioButtonCategory =
                    preferenceScreen.findPreference(PREFERENCE_KEY_RADIO_BUTTON_CATEGORY)!!
                allowRadioPreference =
                    preferenceScreen.findPreference(PREFERENCE_KEY_ALLOW_ACCESS)!!
                denyRadioPreference =
                    preferenceScreen.findPreference(PREFERENCE_KEY_DONT_ALLOW_ACCESS)!!

                allowRadioPreference.setOnClickListener { viewModel.updateAccessState(true) }
                denyRadioPreference.setOnClickListener { viewModel.updateAccessState(false) }
            }
        }
        preferenceFragment.lifecycleScope.launch {
            preferenceFragment.lifecycle.repeatOnLifecycle(State.STARTED) {
                viewModel.uiStateFlow
                    .map { uiState ->
                        when (uiState) {
                            is Stateful.Failure -> Stateful.Failure(throwable = uiState.throwable)
                            is Stateful.Loading -> Stateful.Loading()
                            is Stateful.Success -> {
                                val agentPackageInfo =
                                    getAppFunctionPackageInfoUseCase(
                                        uiState.value.agentPackageName,
                                        requireActivity(),
                                        Process.myUserHandle(),
                                    )!!
                                val targetPackageInfo =
                                    getAppFunctionPackageInfoUseCase(
                                        uiState.value.targetPackageName,
                                        requireActivity(),
                                        Process.myUserHandle(),
                                    )!!
                                Stateful.Success(
                                    ManageAccessRichUiState(
                                        agentPackageInfo,
                                        targetPackageInfo,
                                        uiState.value.accessGranted,
                                    )
                                )
                            }
                        }
                    }
                    .flowOn(Dispatchers.Default)
                    .collect(::onUiStateChanged)
            }
        }
    }

    private fun onUiStateChanged(uiState: Stateful<ManageAccessRichUiState>) {
        when (uiState) {
            // Do nothing
            is Stateful.Loading -> {}
            is Stateful.Failure ->
                Log.e(LOG_TAG, "Failed to manage access settings", uiState.throwable)
            is Stateful.Success -> {
                val preferenceFragment = requirePreferenceFragment()

                val agentLabel = uiState.value.agentPackageInfo.label
                val targetLabel = uiState.value.targetPackageInfo.label

                val title = getString(R.string.app_function_manage_access_title, targetLabel)
                preferenceFragment.setTitle(title)

                val groupTitle =
                    getString(
                        R.string.app_function_manage_access_radiobutton_group_title,
                        agentLabel,
                        targetLabel,
                    )

                introPreference.apply {
                    setTitle(targetLabel)
                    icon = uiState.value.targetPackageInfo.icon
                }
                radioButtonCategory.apply { setTitle(groupTitle) }

                updateCheckedState(uiState.value.accessGranted)
                preferenceFragment.onPreferenceScreenChanged()
            }
        }
    }

    private fun updateCheckedState(isAllowedChecked: Boolean) {
        allowRadioPreference.isChecked = isAllowedChecked
        denyRadioPreference.isChecked = !isAllowedChecked
    }

    @Suppress("UNCHECKED_CAST")
    private fun requirePreferenceFragment(): PF = requireParentFragment() as PF

    /** The data class for UI state of ManageAccess screen that includes drawables and labels. */
    data class ManageAccessRichUiState(
        val agentPackageInfo: AppFunctionPackageInfo,
        val targetPackageInfo: AppFunctionPackageInfo,
        val accessGranted: Boolean,
    )

    /** Interface that the parent fragment must implement. */
    interface Parent {
        /**
         * Set the title of the current settings page.
         *
         * @param title the title of the current settings page
         */
        fun setTitle(title: CharSequence)

        /**
         * Callback when changes have been made to the {@link PreferenceScreen} of the parent {@link
         * PreferenceFragmentCompat}.
         */
        fun onPreferenceScreenChanged()
    }

    companion object {
        private val LOG_TAG = ManageAccessChildFragment::class.java.simpleName

        private const val PREFERENCE_KEY_INTRO = "intro"
        private const val PREFERENCE_KEY_RADIO_BUTTON_CATEGORY = "radio_button_category"
        private const val PREFERENCE_KEY_ALLOW_ACCESS = "allow"
        private const val PREFERENCE_KEY_DONT_ALLOW_ACCESS = "dont_allow"

        /**
         * Create a new instance of ManageAccessChildFragment
         *
         * @param agentPackageName agent package to modify access for
         * @param targetPackageName target package to modify access for
         * @return a new instance of ManageAccessChildFragment
         */
        fun newInstance(
            agentPackageName: String,
            targetPackageName: String,
        ): ManageAccessChildFragment<*> {
            val arguments =
                Bundle().apply {
                    putString(ManageAccessActivity.EXTRA_AGENT_PACKAGE_NAME, agentPackageName)
                    putString(ManageAccessActivity.EXTRA_TARGET_PACKAGE_NAME, targetPackageName)
                }
            return ManageAccessChildFragment<Nothing>().apply { setArguments(arguments) }
        }
    }
}
