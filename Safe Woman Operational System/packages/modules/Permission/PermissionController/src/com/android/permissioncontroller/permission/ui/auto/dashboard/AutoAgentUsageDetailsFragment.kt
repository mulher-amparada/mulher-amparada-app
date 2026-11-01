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

package com.android.permissioncontroller.permission.ui.auto.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.car.ui.preference.CarUiPreference
import com.android.permissioncontroller.DumpableLog
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.R
import com.android.permissioncontroller.appfunctions.ui.viewmodel.v37.AgentUsageDetailsUiState
import com.android.permissioncontroller.appfunctions.ui.viewmodel.v37.AgentUsageDetailsViewModel
import com.android.permissioncontroller.appfunctions.ui.viewmodel.v37.AgentUsageDetailsViewModelFactory
import com.android.permissioncontroller.appinteraction.domain.model.v37.AgentTimelineItem
import com.android.permissioncontroller.auto.AutoSettingsFrameFragment
import com.android.permissioncontroller.permission.ui.auto.AutoDividerPreference
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Fragment for the agent timeline dashboard to be used on Automotive devices. */
@RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
class AutoAgentUsageDetailsFragment : AutoSettingsFrameFragment() {

    companion object {
        private const val LOG_TAG = "AutoAgentUsageDetailsFragment"

        private val MIDNIGHT_TODAY =
            ZonedDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).toEpochSecond() *
                1000L
        private val MIDNIGHT_YESTERDAY =
            ZonedDateTime.now(ZoneId.systemDefault())
                .minusDays(1)
                .truncatedTo(ChronoUnit.DAYS)
                .toEpochSecond() * 1000L

        /** Creates a new instance of [AutoAgentUsageDetailsFragment]. */
        fun newInstance(agentPackageName: String, user: UserHandle): AutoAgentUsageDetailsFragment {
            return AutoAgentUsageDetailsFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(Intent.EXTRA_PACKAGE_NAME, agentPackageName)
                        putParcelable(Intent.EXTRA_USER, user)
                    }
            }
        }
    }

    private lateinit var usageViewModel: AgentUsageDetailsViewModel
    private lateinit var agentPackageName: String
    private lateinit var user: UserHandle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val argumentPackageName = arguments?.getString(Intent.EXTRA_PACKAGE_NAME)
        if (argumentPackageName == null) {
            DumpableLog.e(LOG_TAG, "Missing argument ${Intent.EXTRA_PACKAGE_NAME}")
            activity?.finish()
            return
        }
        val argumentUser = arguments?.getParcelable(Intent.EXTRA_USER, UserHandle::class.java)
        if (argumentUser == null) {
            DumpableLog.e(LOG_TAG, "Missing argument ${Intent.EXTRA_USER}")
            activity?.finish()
            return
        }

        agentPackageName = argumentPackageName
        user = argumentUser
        headerLabel = resources.getString(R.string.permission_usage_agent_activity_title)

        val factory =
            AgentUsageDetailsViewModelFactory(
                PermissionControllerApplication.get(),
                agentPackageName,
                user,
            )

        usageViewModel = ViewModelProvider(this, factory)[AgentUsageDetailsViewModel::class.java]

        usageViewModel.agentUsageDetailsUiDataFlow.onEach { updateUI(it) }.launchIn(lifecycleScope)
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
    }

    private fun setupHeaderPreferences() {
        addTimelineDescriptionPreference()
        preferenceScreen.addPreference(AutoDividerPreference(context))
        // AutoPermissionUsageDetailsFragment has a Manage Permission preference; we don't have one
        // right now for Agents.
    }

    private fun updateUI(uiInfo: AgentUsageDetailsUiState) {
        if (activity == null || uiInfo is AgentUsageDetailsUiState.Loading) {
            return
        }
        preferenceScreen.removeAll()
        setupHeaderPreferences()

        if (uiInfo is AgentUsageDetailsUiState.Failure) {
            return
        }
        val uiData = uiInfo as AgentUsageDetailsUiState.Success

        renderHistoryPreferences(uiData.agentTimelineItems, preferenceScreen)

        setLoading(false)
    }

    fun createAgentHistoryPreference(historyPreferenceData: AgentTimelineItem): Preference {
        return AutoAgentHistoryPreference(requireContext(), historyPreferenceData)
    }

    private fun addTimelineDescriptionPreference() {
        val preference =
            CarUiPreference(context).apply {
                summary = getString(R.string.agent_activity_timeline_category_title_24h)
                isSelectable = false
            }
        preferenceScreen.addPreference(preference)
    }

    /** Render the provided [historyPreferenceDataList] into the [preferenceScreen] UI. */
    private fun renderHistoryPreferences(
        historyPreferenceDataList: List<AgentTimelineItem>,
        preferenceScreen: PreferenceScreen,
    ) {
        historyPreferenceDataList.forEach {
            preferenceScreen.addPreference(createAgentHistoryPreference(it))
        }
    }
}
