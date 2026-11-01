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
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.PreferenceViewHolder
import com.android.permissioncontroller.Constants
import com.android.permissioncontroller.PermissionControllerStatsLog
import com.android.permissioncontroller.PermissionControllerStatsLog.PRIVACY_DASHBOARD_AGENT_TIMELINE_INTERACTION_REPORTED
import com.android.permissioncontroller.PermissionControllerStatsLog.PRIVACY_DASHBOARD_AGENT_TIMELINE_INTERACTION_REPORTED__ACTION__VIEW
import com.android.permissioncontroller.PermissionControllerStatsLog.PRIVACY_DASHBOARD_AGENT_TIMELINE_OPTION_INTERACTION_REPORTED
import com.android.permissioncontroller.PermissionControllerStatsLog.PRIVACY_DASHBOARD_AGENT_TIMELINE_OPTION_INTERACTION_REPORTED__ACTION__SHOW_SEVEN_DAYS_CLICKED
import com.android.permissioncontroller.PermissionControllerStatsLog.PRIVACY_DASHBOARD_AGENT_TIMELINE_OPTION_INTERACTION_REPORTED__ACTION__SHOW_TWENTY_FOUR_HOURS_CLICKED
import com.android.permissioncontroller.PermissionControllerStatsLog.PRIVACY_DASHBOARD_AGENT_TIMELINE_VIEWED
import com.android.permissioncontroller.R
import com.android.permissioncontroller.appfunctions.ui.viewmodel.v37.AgentUsageDetailsUiState
import com.android.permissioncontroller.appfunctions.ui.viewmodel.v37.AgentUsageDetailsViewModel
import com.android.permissioncontroller.appfunctions.ui.viewmodel.v37.AgentUsageDetailsViewModelFactory
import com.android.permissioncontroller.appinteraction.domain.model.v37.AgentTimelineItem
import com.android.permissioncontroller.permission.ui.ManagePermissionsActivity.EXTRA_SHOW_7_DAYS
import com.android.permissioncontroller.permission.ui.handheld.SettingsWithLargeHeader
import com.android.permissioncontroller.permission.utils.KotlinUtils
import com.android.settingslib.widget.FooterPreference
import com.android.settingslib.widget.TopIntroPreference
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.properties.Delegates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class AgentUsageDetailsFragment : SettingsWithLargeHeader() {
    private lateinit var agentPackageName: String
    private lateinit var user: UserHandle
    private var sessionId: Long by Delegates.notNull()

    private lateinit var viewModel: AgentUsageDetailsViewModel

    private lateinit var show7DaysMenuItem: MenuItem
    private lateinit var show24HoursMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val argumentPackageName = arguments?.getString(Intent.EXTRA_PACKAGE_NAME)
        if (argumentPackageName == null) {
            Log.e(
                LOG_TAG,
                "No agent package name was provided. This is mandatory" +
                    " for creating AgentUsageDetailsFragment",
            )
            requireActivity().finish()
            return
        }
        val argumentUser = arguments?.getParcelable(Intent.EXTRA_USER, UserHandle::class.java)
        if (argumentUser == null) {
            Log.e(
                LOG_TAG,
                "No user id was provided. This is mandatory for creating AgentUsageDetailsFragment",
            )
            requireActivity().finish()
            return
        }

        agentPackageName = argumentPackageName
        user = argumentUser
        sessionId = arguments?.getLong(Constants.EXTRA_SESSION_ID)!!
        val factory =
            AgentUsageDetailsViewModelFactory(requireActivity().application, agentPackageName, user)
        viewModel = ViewModelProvider(this, factory)[AgentUsageDetailsViewModel::class.java]
        viewModel.updateShow7DaysToggle(arguments?.getBoolean(EXTRA_SHOW_7_DAYS) ?: false)

        val agentLabel =
            KotlinUtils.getPackageLabel(requireActivity().application, agentPackageName, user)
        val title = resources.getString(R.string.agent_activity_timeline_title, agentLabel)
        requireActivity().setTitle(title)

        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.agentUsageDetailsUiDataFlow
                    .flowOn(Dispatchers.Default)
                    .collect(::onUiStateChanged)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .addMenuProvider(
                object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, inflator: MenuInflater) {
                        show7DaysMenuItem =
                            menu.add(
                                Menu.NONE,
                                MENU_SHOW_7_DAYS,
                                Menu.NONE,
                                R.string.menu_show_7_days_data,
                            )
                        show24HoursMenuItem =
                            menu.add(
                                Menu.NONE,
                                MENU_SHOW_24_HOURS,
                                Menu.NONE,
                                R.string.menu_show_24_hours_data,
                            )
                    }

                    override fun onPrepareMenu(menu: Menu) {
                        if (
                            !::show7DaysMenuItem.isInitialized ||
                                !::show24HoursMenuItem.isInitialized
                        ) {
                            return
                        }
                        show7DaysMenuItem.setVisible(!viewModel.getShow7Days())
                        show24HoursMenuItem.setVisible(viewModel.getShow7Days())
                    }

                    override fun onMenuItemSelected(item: MenuItem): Boolean =
                        when (item.itemId) {
                            android.R.id.home -> {
                                requireActivity().finishAfterTransition()
                                true
                            }
                            MENU_SHOW_7_DAYS -> {
                                viewModel.updateShow7DaysToggle(true)
                                PermissionControllerStatsLog.write(
                                    PRIVACY_DASHBOARD_AGENT_TIMELINE_OPTION_INTERACTION_REPORTED,
                                    sessionId,
                                    PRIVACY_DASHBOARD_AGENT_TIMELINE_OPTION_INTERACTION_REPORTED__ACTION__SHOW_SEVEN_DAYS_CLICKED,
                                )
                                true
                            }
                            MENU_SHOW_24_HOURS -> {
                                viewModel.updateShow7DaysToggle(false)
                                PermissionControllerStatsLog.write(
                                    PRIVACY_DASHBOARD_AGENT_TIMELINE_OPTION_INTERACTION_REPORTED,
                                    sessionId,
                                    PRIVACY_DASHBOARD_AGENT_TIMELINE_OPTION_INTERACTION_REPORTED__ACTION__SHOW_TWENTY_FOUR_HOURS_CLICKED,
                                )
                                true
                            }
                            else -> false
                        }
                },
                viewLifecycleOwner,
            )
    }

    private fun onUiStateChanged(uiState: AgentUsageDetailsUiState) {
        if (preferenceScreen == null) {
            preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
            setPreferenceScreen(preferenceScreen)
        }
        preferenceScreen.removeAll()

        when (uiState) {
            is AgentUsageDetailsUiState.Loading -> {
                setLoading(true, false)
            }
            is AgentUsageDetailsUiState.Failure -> {
                Log.e(LOG_TAG, "Failed to load agent activities", uiState.throwable)
                setLoading(false, true)
            }
            is AgentUsageDetailsUiState.Success -> {
                addTopIntro(preferenceScreen)
                if (uiState.show7Days) {
                    addAgentActivityPreferencesForPast7Days(
                        uiState.settingsPackageName,
                        uiState.agentTimelineItems,
                        preferenceScreen,
                    )
                } else {
                    addAgentActivityPreferencesForPast24Hours(
                        uiState.settingsPackageName,
                        uiState.agentTimelineItems,
                        preferenceScreen,
                    )
                }
                addFooter(preferenceScreen)
                setLoading(false, true)

                PermissionControllerStatsLog.write(
                    PRIVACY_DASHBOARD_AGENT_TIMELINE_VIEWED,
                    sessionId,
                    uiState.agentUid,
                    uiState.agentPackageName,
                    uiState.show7Days,
                )
            }
        }
    }

    fun addAgentActivityPreferencesForPast24Hours(
        settingsAppPackageName: String,
        agentTimelineItems: List<AgentTimelineItem>,
        preferenceScreen: PreferenceScreen,
    ) {
        val last24Hours =
            (ZonedDateTime.now(ZoneId.systemDefault()).minusDays(1).toEpochSecond() * 1000L)
        val category = PreferenceCategory(requireContext())
        category.title = resources.getString(R.string.agent_activity_timeline_category_title_24h)
        preferenceScreen.addPreference(category)

        if (agentTimelineItems.isEmpty()) {
            category.addPreference(
                Preference(requireContext()).apply {
                    title =
                        resources.getString(R.string.empty_agent_activity_timeline_preference_title)
                    isSelectable = false
                }
            )
        } else {
            for (uiInfo: AgentTimelineItem in agentTimelineItems) {
                val accessTime = uiInfo.lastAccessTime
                if (accessTime < last24Hours) {
                    continue
                }
                category.addPreference(
                    createAgentActivityPreference(uiInfo, settingsAppPackageName)
                )
            }
        }
    }

    fun addAgentActivityPreferencesForPast7Days(
        settingsAppPackageName: String,
        agentTimelineItems: List<AgentTimelineItem>,
        preferenceScreen: PreferenceScreen,
    ) {
        val midnightToday =
            (ZonedDateTime.now(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS)
                .toEpochSecond() * 1000L)
        val midnightYesterday =
            (ZonedDateTime.now(ZoneId.systemDefault())
                .minusDays(1)
                .truncatedTo(ChronoUnit.DAYS)
                .toEpochSecond() * 1000L)
        var previousAccessDate: Long? = null
        var category = PreferenceCategory(requireContext())

        if (agentTimelineItems.isEmpty()) {
            preferenceScreen.addPreference(category)
            category.title = resources.getString(R.string.agent_activity_timeline_category_title_7d)
            category.addPreference(
                Preference(requireContext()).apply {
                    title =
                        resources.getString(R.string.empty_agent_activity_timeline_preference_title)
                    isSelectable = false
                }
            )
        } else {
            for (uiInfo: AgentTimelineItem in agentTimelineItems) {
                val accessTime = uiInfo.lastAccessTime
                val accessDate =
                    ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(accessTime),
                            ZoneId.systemDefault(),
                        )
                        .truncatedTo(ChronoUnit.DAYS)
                        .toEpochSecond() * 1000L
                if (previousAccessDate == null || accessDate != previousAccessDate) {
                    val categoryTitle =
                        if (accessTime > midnightToday) {
                            resources.getString(
                                R.string.agent_activity_timeline_category_title_today
                            )
                        } else if (accessTime > midnightYesterday) {
                            resources.getString(
                                R.string.agent_activity_timeline_category_title_yesterday
                            )
                        } else {
                            DateFormat.getLongDateFormat(requireContext()).format(accessDate)
                        }
                    previousAccessDate = accessDate

                    category = PreferenceCategory(requireContext())
                    category.title = categoryTitle
                    preferenceScreen.addPreference(category)
                }

                category.addPreference(
                    createAgentActivityPreference(uiInfo, settingsAppPackageName)
                )
            }
        }
    }

    fun createAgentActivityPreference(
        uiInfo: AgentTimelineItem,
        settingsAppPackageName: String,
    ): Preference =
        object : Preference(requireContext()) {
                override fun onBindViewHolder(holder: PreferenceViewHolder) {
                    super.onBindViewHolder(holder)

                    if (uiInfo.interactionUri != null) {
                        holder.findViewById(R.id.agent_activity_widget)!!.setOnClickListener { _ ->
                            val intent = Intent(Intent.ACTION_VIEW, uiInfo.interactionUri.toUri())
                            intent.setPackage(uiInfo.agentPackageName)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            PermissionControllerStatsLog.write(
                                PRIVACY_DASHBOARD_AGENT_TIMELINE_INTERACTION_REPORTED,
                                sessionId,
                                uiInfo.agentUid,
                                uiInfo.agentPackageName,
                                uiInfo.targetPackageName,
                                PRIVACY_DASHBOARD_AGENT_TIMELINE_INTERACTION_REPORTED__ACTION__VIEW,
                            )
                            startActivity(intent)
                        }
                    }
                }
            }
            .apply {
                title =
                    if (uiInfo.isDeviceAssistanceAccess) {
                        resources.getString(R.string.device_assistance_title)
                    } else {
                        KotlinUtils.getPackageLabel(
                            requireActivity().application,
                            uiInfo.targetPackageName,
                            uiInfo.user,
                        )
                    }
                val iconPackageName =
                    if (uiInfo.isDeviceAssistanceAccess) {
                        settingsAppPackageName
                    } else {
                        uiInfo.targetPackageName
                    }
                icon =
                    KotlinUtils.getBadgedPackageIcon(
                        requireActivity().application,
                        iconPackageName,
                        uiInfo.user,
                    )
                summary =
                    resources.getString(
                        R.string.agent_activity_timeline_activity_summary,
                        DateFormat.getTimeFormat(requireContext()).format(uiInfo.lastAccessTime),
                    )
                if (uiInfo.interactionUri != null) {
                    widgetLayoutResource = R.layout.agent_activity_preference_widget
                }
                // The preference is not clickable. Hence setting this to prevent ripple effect when
                // clicking on the preference
                isSelectable = false
            }

    fun addTopIntro(preferenceScreen: PreferenceScreen) {
        val topIntroPreference =
            TopIntroPreference(requireContext()).apply {
                title =
                    resources.getString(
                        R.string.agent_activity_timeline_top_intro_title,
                        KotlinUtils.getPackageLabel(
                            requireActivity().application,
                            agentPackageName,
                            user,
                        ),
                    )
            }
        preferenceScreen.addPreference(topIntroPreference)
    }

    fun addFooter(preferenceScreen: PreferenceScreen) {
        val footerPreference =
            FooterPreference(requireContext()).apply {
                icon = requireContext().getDrawable(R.drawable.ic_info_outline)
                title = resources.getString(R.string.agent_activity_timeline_footer_title)
            }
        preferenceScreen.addPreference(footerPreference)
    }

    companion object {
        private val LOG_TAG = AgentUsageDetailsFragment::class.java.simpleName

        private const val MENU_SHOW_7_DAYS = Menu.FIRST + 4
        private const val MENU_SHOW_24_HOURS = Menu.FIRST + 5
    }
}
