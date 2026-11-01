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
import android.graphics.drawable.Drawable
import android.icu.text.Collator
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
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import com.android.permissioncontroller.R
import com.android.permissioncontroller.appfunctions.domain.model.v31.AppFunctionPackageInfo
import com.android.permissioncontroller.appfunctions.domain.usecase.GetAppFunctionPackageInfoUseCaseImpl
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAppFunctionPackageInfoUseCase
import com.android.permissioncontroller.appfunctions.ui.viewmodel.TargetAccessViewModel
import com.android.permissioncontroller.appfunctions.ui.viewmodel.TargetAccessViewModelFactory
import com.android.permissioncontroller.common.model.Stateful
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository
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
class TargetAccessChildFragment<PF>() : Fragment(), Preference.OnPreferenceClickListener
    where PF : PreferenceFragmentCompat, PF : TargetAccessChildFragment.Parent {
    private lateinit var targetPackageName: String

    private lateinit var viewModel: TargetAccessViewModel
    private lateinit var getAppFunctionPackageInfoUseCase: GetAppFunctionPackageInfoUseCase
    private lateinit var agentListComparator: Comparator<AppFunctionPackageInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        targetPackageName = arguments!!.getString(Intent.EXTRA_PACKAGE_NAME)!!

        val factory = TargetAccessViewModelFactory(requireActivity().application, targetPackageName)
        viewModel = ViewModelProvider(this, factory).get(TargetAccessViewModel::class.java)

        val packageRepository = PackageRepository.createInstance(requireContext())
        getAppFunctionPackageInfoUseCase = GetAppFunctionPackageInfoUseCaseImpl(packageRepository)

        val collator =
            Collator.getInstance(
                requireActivity().application.resources.configuration.getLocales().get(0)
            )
        agentListComparator = compareBy(collator) { it.label }

        val preferenceFragment = requirePreferenceFragment()
        preferenceFragment.lifecycleScope.launch {
            preferenceFragment.lifecycle.repeatOnLifecycle(State.STARTED) {
                viewModel.uiStateFlow
                    .map { uiState ->
                        when (uiState) {
                            is Stateful.Failure -> Stateful.Failure(throwable = uiState.throwable)
                            is Stateful.Loading -> Stateful.Loading()
                            is Stateful.Success -> {
                                val targetPackageInfo =
                                    getAppFunctionPackageInfoUseCase(
                                        uiState.value.targetPackageName,
                                        requireContext(),
                                        Process.myUserHandle(),
                                    )!!
                                val allowedAgentPackageInfos =
                                    uiState.value.allowedAgentPackageNames
                                        .map {
                                            getAppFunctionPackageInfoUseCase(
                                                it,
                                                requireContext(),
                                                Process.myUserHandle(),
                                            )!!
                                        }
                                        .sortedWith(agentListComparator)
                                val notAllowedAgentPackageInfos =
                                    uiState.value.notAllowedAgentPackageNames
                                        .map {
                                            getAppFunctionPackageInfoUseCase(
                                                it,
                                                requireContext(),
                                                Process.myUserHandle(),
                                            )!!
                                        }
                                        .sortedWith(agentListComparator)
                                Stateful.Success(
                                    TargetAccessRichUiState(
                                        targetPackageInfo,
                                        allowedAgentPackageInfos,
                                        notAllowedAgentPackageInfos,
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

    private fun onUiStateChanged(uiState: Stateful<TargetAccessRichUiState>) {
        if (uiState is Stateful.Loading) {
            // do nothing
            return
        } else if (uiState is Stateful.Failure) {
            Log.e(LOG_TAG, "Failed to load agent list", uiState.throwable)
        }

        val preferenceFragment = requirePreferenceFragment()
        val preferenceManager = preferenceFragment.preferenceManager
        var preferenceScreen = preferenceFragment.preferenceScreen
        val context = preferenceManager.context
        val oldPreferences = mutableMapOf<String, Preference>()

        var oldAllowedAgentsPreferenceCategory: PreferenceCategory? = null
        var oldNotAllowedAgentsPreferenceCategory: PreferenceCategory? = null
        val oldAgentsPreferences = mutableMapOf<String, Preference>()

        if (preferenceScreen == null) {
            preferenceScreen = preferenceManager.createPreferenceScreen(context)
            preferenceFragment.preferenceScreen = preferenceScreen
        } else {
            oldAllowedAgentsPreferenceCategory =
                preferenceScreen.findPreference(PREFERENCE_KEY_ALLOWED_CATEGORY)
            clearPreferenceCategory(oldAllowedAgentsPreferenceCategory, oldAgentsPreferences)

            oldNotAllowedAgentsPreferenceCategory =
                preferenceScreen.findPreference(PREFERENCE_KEY_NOT_ALLOWED_CATEGORY)
            clearPreferenceCategory(oldNotAllowedAgentsPreferenceCategory, oldAgentsPreferences)

            clearPreferences(preferenceScreen, oldPreferences)
        }

        val targetLabel = uiState.value?.targetPackageInfo?.label ?: targetPackageName
        val targetIcon = uiState.value?.targetPackageInfo?.icon
        val allowedAgents = uiState.value?.allowedAgentPackageInfos ?: emptyList()
        val notAllowedAgents = uiState.value?.notAllowedAgentPackageInfos ?: emptyList()

        addHeaderPreference(preferenceScreen, targetLabel, targetIcon, oldPreferences)
        addAllowedAgentsPreferenceCategory(
            oldAllowedAgentsPreferenceCategory,
            preferenceScreen,
            allowedAgents,
            oldAgentsPreferences,
            context,
        )
        addNotAllowedAgentsPreferenceCategory(
            oldNotAllowedAgentsPreferenceCategory,
            preferenceScreen,
            notAllowedAgents,
            oldAgentsPreferences,
            context,
        )

        preferenceFragment.onPreferenceScreenChanged()
    }

    private fun clearPreferenceCategory(
        preferenceCategory: PreferenceCategory?,
        oldPreferences: MutableMap<String, Preference>,
    ) {
        if (preferenceCategory == null) {
            return
        }
        clearPreferences(preferenceCategory, oldPreferences)
        preferenceCategory.parent?.removePreference(preferenceCategory)
        preferenceCategory.order = Preference.DEFAULT_ORDER
    }

    private fun clearPreferences(
        preferenceGroup: PreferenceGroup,
        oldPreferences: MutableMap<String, Preference>,
    ) {
        for (i in preferenceGroup.preferenceCount - 1 downTo 0) {
            val preference = preferenceGroup.getPreference(i)
            preferenceGroup.removePreference(preference)
            preference.order = Preference.DEFAULT_ORDER
            oldPreferences[preference.key] = preference
        }
    }

    private fun addHeaderPreference(
        preferenceGroup: PreferenceGroup,
        targetLabel: String,
        targetIcon: Drawable?,
        oldPreferences: Map<String, Preference>,
    ) {
        val preference =
            oldPreferences[PREFERENCE_KEY_INTRO]
                ?: requirePreferenceFragment().createHeaderPreference().apply {
                    key = PREFERENCE_KEY_INTRO
                    setSummary(R.string.app_function_target_access_summary)
                }
        // Old preference might need to be updated
        preference.apply {
            icon = targetIcon
            title = targetLabel
        }
        preferenceGroup.addPreference(preference)
    }

    private fun addAllowedAgentsPreferenceCategory(
        oldPreferenceCategory: PreferenceCategory?,
        preferenceScreen: PreferenceScreen,
        agents: List<AppFunctionPackageInfo>,
        oldPreferences: Map<String, Preference>,
        context: Context,
    ) {
        val preferenceCategory =
            oldPreferenceCategory
                ?: PreferenceCategory(context).apply {
                    key = PREFERENCE_KEY_ALLOWED_CATEGORY
                    setTitle(R.string.app_function_access_allowed_header)
                }
        preferenceScreen.addPreference(preferenceCategory)

        if (agents.isEmpty()) {
            addEmptyStatePreference(
                preferenceCategory,
                PREFERENCE_KEY_ZERO_STATE_ALLOWED,
                R.string.app_function_agent_access_none_allowed_title,
                oldPreferences,
            )
        } else {
            addAgentsPreference(preferenceCategory, agents, oldPreferences)
        }
    }

    private fun addNotAllowedAgentsPreferenceCategory(
        oldPreferenceCategory: PreferenceCategory?,
        preferenceScreen: PreferenceScreen,
        agents: List<AppFunctionPackageInfo>,
        oldPreferences: Map<String, Preference>,
        context: Context,
    ) {
        val preferenceCategory =
            oldPreferenceCategory
                ?: PreferenceCategory(context).apply {
                    key = PREFERENCE_KEY_NOT_ALLOWED_CATEGORY
                    setTitle(R.string.app_function_access_denied_header)
                }
        preferenceScreen.addPreference(preferenceCategory)

        if (agents.isEmpty()) {
            addEmptyStatePreference(
                preferenceCategory,
                PREFERENCE_KEY_ZERO_STATE_NOT_ALLOWED,
                R.string.app_function_agent_access_none_denied_title,
                oldPreferences,
            )
        } else {
            addAgentsPreference(preferenceCategory, agents, oldPreferences)
        }
    }

    private fun addEmptyStatePreference(
        preferenceGroup: PreferenceGroup,
        preferenceKey: String,
        titleResId: Int,
        oldPreferences: Map<String, Preference>,
    ) {
        val preference =
            oldPreferences[preferenceKey]
                ?: requirePreferenceFragment().createPreference().apply {
                    key = preferenceKey
                    setTitle(titleResId)
                }
        preferenceGroup.addPreference(preference)
    }

    private fun addAgentsPreference(
        preferenceGroup: PreferenceGroup,
        agents: List<AppFunctionPackageInfo>,
        oldPreferences: Map<String, Preference>,
    ) {
        val preferenceFragment = requirePreferenceFragment()
        for (agent in agents) {
            val agentPackageName = agent.packageName
            val preference =
                oldPreferences[agentPackageName]
                    ?: preferenceFragment.createPreference().apply {
                        key = agentPackageName
                        onPreferenceClickListener = this@TargetAccessChildFragment
                    }
            // Ensure current ux state reflects the data state
            preference.apply {
                title = agent.label
                icon = agent.icon
            }
            preferenceGroup.addPreference(preference)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val agentPackageName = preference.key
        val intent =
            ManageAccessActivity.createIntent(requireContext(), agentPackageName, targetPackageName)
        startActivity(intent)
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun requirePreferenceFragment(): PF = requireParentFragment() as PF

    /** The data class for UI state of ManageAccess screen that includes drawables and labels. */
    data class TargetAccessRichUiState(
        val targetPackageInfo: AppFunctionPackageInfo,
        val allowedAgentPackageInfos: List<AppFunctionPackageInfo> = emptyList(),
        val notAllowedAgentPackageInfos: List<AppFunctionPackageInfo> = emptyList(),
    )

    /** Interface that the parent fragment must implement. */
    interface Parent {
        /** Creates a new header preference for the screen */
        fun createHeaderPreference(): Preference

        /** Creates a new preference for a target app. */
        fun createPreference(): Preference

        /**
         * Callback when changes have been made to the {@link PreferenceScreen} of the parent {@link
         * PreferenceFragmentCompat}.
         */
        fun onPreferenceScreenChanged()
    }

    companion object {
        private val LOG_TAG = TargetAccessChildFragment::class.java.simpleName
        private val PREFERENCE_KEY_INTRO =
            TargetAccessChildFragment::class.java.name + ".preference.INTRO"
        private val PREFERENCE_KEY_ALLOWED_CATEGORY =
            TargetAccessChildFragment::class.java.name + ".preference.ALLOWED_CATEGORY"
        private val PREFERENCE_KEY_NOT_ALLOWED_CATEGORY =
            TargetAccessChildFragment::class.java.name + ".preference.NOT_ALLOWED_CATEGORY"
        private val PREFERENCE_KEY_ZERO_STATE_ALLOWED =
            TargetAccessChildFragment::class.java.name + ".preference.ZERO_STATE_ALLOWED"
        private val PREFERENCE_KEY_ZERO_STATE_NOT_ALLOWED =
            TargetAccessChildFragment::class.java.name + ".preference.ZERO_STATE_NOT_ALLOWED"

        /**
         * Create a new instance of TargetAccessChildFragment
         *
         * @param targetPackageName target package to modify access for
         * @return a new instance of TargetAccessChildFragment
         */
        fun newInstance(targetPackageName: String): TargetAccessChildFragment<*> {
            val arguments =
                Bundle().apply { putString(Intent.EXTRA_PACKAGE_NAME, targetPackageName) }
            return TargetAccessChildFragment<Nothing>().apply { setArguments(arguments) }
        }
    }
}
