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
import com.android.permissioncontroller.appfunctions.ui.viewmodel.AgentAccessViewModel
import com.android.permissioncontroller.appfunctions.ui.viewmodel.AgentAccessViewModelFactory
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
class AgentAccessChildFragment<PF> : Fragment(), Preference.OnPreferenceClickListener
    where PF : PreferenceFragmentCompat, PF : AgentAccessChildFragment.Parent {
    private lateinit var agentPackageName: String

    private lateinit var viewModel: AgentAccessViewModel
    private lateinit var getAppFunctionPackageInfoUseCase: GetAppFunctionPackageInfoUseCase
    private lateinit var targetListComparator: Comparator<AppFunctionPackageInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        agentPackageName = arguments!!.getString(Intent.EXTRA_PACKAGE_NAME)!!

        val factory = AgentAccessViewModelFactory(requireActivity().application, agentPackageName)
        viewModel = ViewModelProvider(this, factory).get(AgentAccessViewModel::class.java)

        val packageRepository = PackageRepository.createInstance(requireContext())
        getAppFunctionPackageInfoUseCase = GetAppFunctionPackageInfoUseCaseImpl(packageRepository)

        val collator =
            Collator.getInstance(
                requireActivity().application.resources.configuration.getLocales().get(0)
            )
        targetListComparator = compareBy(collator) { it.label }

        val preferenceFragment = requirePreferenceFragment()
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
                                        requireContext(),
                                        Process.myUserHandle(),
                                    )!!
                                val allowedTargets =
                                    uiState.value.allowedTargetPackageNames
                                        .map {
                                            getAppFunctionPackageInfoUseCase(
                                                it,
                                                requireContext(),
                                                Process.myUserHandle(),
                                            )!!
                                        }
                                        .sortedWith(targetListComparator)
                                val notAllowedTargets =
                                    uiState.value.notAllowedTargetPackageNames
                                        .map {
                                            getAppFunctionPackageInfoUseCase(
                                                it,
                                                requireContext(),
                                                Process.myUserHandle(),
                                            )!!
                                        }
                                        .sortedWith(targetListComparator)
                                Stateful.Success(
                                    AgentAccessRichUiState(
                                        agentPackageInfo,
                                        allowedTargets,
                                        notAllowedTargets,
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

    private fun onUiStateChanged(uiState: Stateful<AgentAccessRichUiState>) {
        if (uiState is Stateful.Loading) {
            // do nothing
            return
        } else if (uiState is Stateful.Failure) {
            Log.e(LOG_TAG, "Failed to load target list", uiState.throwable)
        }

        val preferenceFragment = requirePreferenceFragment()
        val preferenceManager = preferenceFragment.preferenceManager
        var preferenceScreen = preferenceFragment.preferenceScreen
        val context = preferenceManager.context
        val oldPreferences = mutableMapOf<String, Preference>()

        var oldAllowedTargetPreferenceCategory: PreferenceCategory? = null
        var oldNotAllowedTargetPreferenceCategory: PreferenceCategory? = null
        val oldAppTargetPreferences = mutableMapOf<String, Preference>()

        if (preferenceScreen == null) {
            preferenceScreen = preferenceManager.createPreferenceScreen(context)
            preferenceFragment.preferenceScreen = preferenceScreen
        } else {
            oldAllowedTargetPreferenceCategory =
                preferenceScreen.findPreference(PREFERENCE_KEY_ALLOWED_CATEGORY)
            clearPreferenceCategory(oldAllowedTargetPreferenceCategory, oldAppTargetPreferences)

            oldNotAllowedTargetPreferenceCategory =
                preferenceScreen.findPreference(PREFERENCE_KEY_NOT_ALLOWED_CATEGORY)
            clearPreferenceCategory(oldNotAllowedTargetPreferenceCategory, oldAppTargetPreferences)

            clearPreferences(preferenceScreen, oldPreferences)
        }

        val agentLabel = uiState.value?.agent?.label ?: agentPackageName
        val agentIcon = uiState.value?.agent?.icon
        val allowedTargets = uiState.value?.allowedTargets ?: emptyList()
        val notAllowedTargets = uiState.value?.notAllowedTargets ?: emptyList()

        addHeaderPreference(preferenceScreen, agentLabel, agentIcon, oldPreferences)
        addAllowedTargetsPreferenceCategory(
            oldAllowedTargetPreferenceCategory,
            preferenceScreen,
            allowedTargets,
            oldAppTargetPreferences,
            context,
        )
        addNotAllowedTargetsPreferenceCategory(
            oldNotAllowedTargetPreferenceCategory,
            preferenceScreen,
            notAllowedTargets,
            oldAppTargetPreferences,
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
        agentLabel: String,
        agentIcon: Drawable?,
        oldPreferences: Map<String, Preference>,
    ) {
        val preference =
            oldPreferences[PREFERENCE_KEY_INTRO]
                ?: requirePreferenceFragment().createHeaderPreference().apply {
                    key = PREFERENCE_KEY_INTRO
                    setSummary(R.string.app_function_agent_access_summary)
                }
        // Old preference might need to be updated
        preference.apply {
            icon = agentIcon
            title = agentLabel
        }
        preferenceGroup.addPreference(preference)
    }

    private fun addAllowedTargetsPreferenceCategory(
        oldPreferenceCategory: PreferenceCategory?,
        preferenceScreen: PreferenceScreen,
        targets: List<AppFunctionPackageInfo>,
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

        if (targets.isEmpty()) {
            addEmptyStatePreference(
                preferenceCategory,
                PREFERENCE_KEY_ZERO_STATE_ALLOWED,
                R.string.app_function_target_access_none_allowed_title,
                oldPreferences,
            )
        } else {
            addTargetPreferences(preferenceCategory, targets, oldPreferences)
        }
    }

    private fun addNotAllowedTargetsPreferenceCategory(
        oldPreferenceCategory: PreferenceCategory?,
        preferenceScreen: PreferenceScreen,
        targets: List<AppFunctionPackageInfo>,
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

        if (targets.isEmpty()) {
            addEmptyStatePreference(
                preferenceCategory,
                PREFERENCE_KEY_ZERO_STATE_NOT_ALLOWED,
                R.string.app_function_target_access_none_denied_title,
                oldPreferences,
            )
        } else {
            addTargetPreferences(preferenceCategory, targets, oldPreferences)
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

    private fun addTargetPreferences(
        preferenceGroup: PreferenceGroup,
        targets: List<AppFunctionPackageInfo>,
        oldPreferences: Map<String, Preference>,
    ) {
        val preferenceFragment = requirePreferenceFragment()
        for (target in targets) {
            val targetPackageName = target.packageName
            val preference =
                oldPreferences[targetPackageName]
                    ?: preferenceFragment.createPreference().apply {
                        key = targetPackageName
                        onPreferenceClickListener = this@AgentAccessChildFragment
                    }
            // Ensure current ux state reflects the data state
            preference.apply {
                title = target.label
                icon = target.icon
            }
            preferenceGroup.addPreference(preference)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val targetPackageName = preference.key
        val intent =
            ManageAccessActivity.createIntent(requireContext(), agentPackageName, targetPackageName)
        startActivity(intent)
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun requirePreferenceFragment(): PF = requireParentFragment() as PF

    /** The data class for UI state of ManageAccess screen that includes drawables and labels. */
    data class AgentAccessRichUiState(
        val agent: AppFunctionPackageInfo,
        val allowedTargets: List<AppFunctionPackageInfo> = emptyList(),
        val notAllowedTargets: List<AppFunctionPackageInfo> = emptyList(),
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
        private val LOG_TAG = AgentAccessChildFragment::class.java.simpleName
        private val PREFERENCE_KEY_INTRO =
            AgentAccessChildFragment::class.java.name + ".preference.INTRO"
        private val PREFERENCE_KEY_ALLOWED_CATEGORY =
            AgentAccessChildFragment::class.java.name + ".preference.ALLOWED_CATEGORY"
        private val PREFERENCE_KEY_NOT_ALLOWED_CATEGORY =
            AgentAccessChildFragment::class.java.name + ".preference.NOT_ALLOWED_CATEGORY"
        private val PREFERENCE_KEY_ZERO_STATE_ALLOWED =
            AgentAccessChildFragment::class.java.name + ".preference.ZERO_STATE_ALLOWED"
        private val PREFERENCE_KEY_ZERO_STATE_NOT_ALLOWED =
            AgentAccessChildFragment::class.java.name + ".preference.ZERO_STATE_NOT_ALLOWED"

        /**
         * Create a new instance of AgentAccessChildFragment
         *
         * @param agentPackageName agent package to modify access for
         * @return a new instance of AgentAccessChildFragment
         */
        fun newInstance(agentPackageName: String): AgentAccessChildFragment<*> {
            val arguments =
                Bundle().apply { putString(Intent.EXTRA_PACKAGE_NAME, agentPackageName) }
            return AgentAccessChildFragment<Nothing>().apply { setArguments(arguments) }
        }
    }
}
