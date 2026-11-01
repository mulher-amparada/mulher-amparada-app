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

package com.android.permissioncontroller.tests.mocking.appfunctions.ui.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.CheckFlagsRule
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.appfunctions.domain.usecase.v37.GetAgentUsageDetailsUseCase
import com.android.permissioncontroller.appfunctions.ui.viewmodel.v37.AgentUsageDetailsUiState
import com.android.permissioncontroller.appfunctions.ui.viewmodel.v37.AgentUsageDetailsViewModel
import com.android.permissioncontroller.appinteraction.domain.model.v37.AccessHistory
import com.android.permissioncontroller.flags.Flags
import com.android.permissioncontroller.tests.mocking.appinteraction.data.repository.FakeAppInteractionRepository
import com.android.permissioncontroller.tests.mocking.coroutines.collectLastValue
import com.android.permissioncontroller.tests.mocking.pm.data.repository.FakePackageRepository
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations

/** Unit tests for [AgentUsageDetailsViewModel]. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
class AgentUsageDetailsViewModelTest {
    @get:Rule val checkFlagsRule: CheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()
    @Mock private lateinit var application: PermissionControllerApplication
    @Mock private lateinit var context: Context
    @Mock private lateinit var packageManager: PackageManager
    @Mock private lateinit var userHandle: UserHandle

    private val instrumentation = InstrumentationRegistry.getInstrumentation()!!
    private val instrumentationContext = instrumentation.targetContext!!
    private val now = System.currentTimeMillis()
    private val defaultAccessHistory =
        listOf(
            createAccessHistory(
                AGENT_NAME_1,
                TARGET_NAME_1,
                INTERACTION_URI_1,
                now - TimeUnit.HOURS.toMillis(1),
            ),
            createAccessHistory(
                AGENT_NAME_1,
                TARGET_NAME_2,
                INTERACTION_URI_2,
                now - TimeUnit.HOURS.toMillis(2),
            ),
            createAccessHistory(
                AGENT_NAME_1,
                TARGET_NAME_3,
                INTERACTION_URI_3,
                now - TimeUnit.DAYS.toMillis(3),
            ),
            createAccessHistory(
                AGENT_NAME_2,
                TARGET_NAME_1,
                INTERACTION_URI_1,
                now - TimeUnit.HOURS.toMillis(3),
            ),
        )

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        whenever(application.applicationContext).thenReturn(context)
        whenever(context.packageManager).thenReturn(packageManager)
        whenever(packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE))
            .thenReturn(false)
        whenever(packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)).thenReturn(false)
        whenever(packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)).thenReturn(false)
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun verifyAgentUsagesDetailsAreShownForPast24Hours() = runTest {
        assumeFalse(isTv() || isWatch())
        val viewModel =
            getViewModel(defaultAccessHistory, SavedStateHandle(mapOf("show7Days" to false)))
        val uiState = getAgentUsageDetailsUiState(viewModel)
        assertThat(uiState.agentPackageName).isEqualTo(AGENT_NAME_1)
        assertWithMessage("There should be 2 accesses in the past 24 hours")
            .that(uiState.agentTimelineItems.size)
            .isEqualTo(2)
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun verifyAgentUsagesDetailsAreShownForPast7Days() = runTest {
        assumeFalse(isTv() || isWatch())
        val viewModel =
            getViewModel(defaultAccessHistory, SavedStateHandle(mapOf("show7Days" to true)))
        val uiState = getAgentUsageDetailsUiState(viewModel)
        assertThat(uiState.agentPackageName).isEqualTo(AGENT_NAME_1)
        assertWithMessage("There should be 3 accesses in the past 7 days")
            .that(uiState.agentTimelineItems.size)
            .isEqualTo(3)
    }

    private fun createAccessHistory(
        agentPackageName: String,
        targetPackageName: String,
        interactionUri: String,
        accessTime: Long,
    ) = AccessHistory(agentPackageName, targetPackageName, null, null, interactionUri, accessTime)

    private fun TestScope.getAgentUsageDetailsUiState(
        viewModel: AgentUsageDetailsViewModel
    ): AgentUsageDetailsUiState.Success {
        val result by collectLastValue(viewModel.agentUsageDetailsUiDataFlow)
        return result as AgentUsageDetailsUiState.Success
    }

    private fun TestScope.getViewModel(
        accessHistory: List<AccessHistory> = emptyList(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(emptyMap()),
    ): AgentUsageDetailsViewModel {
        val getAppFunctionAgentUsageDetailsUseCase =
            GetAgentUsageDetailsUseCase(
                FakeAppInteractionRepository(accessHistory),
                FakePackageRepository(),
            )
        return AgentUsageDetailsViewModel(
            application,
            AGENT_NAME_1,
            userHandle,
            FakePackageRepository(settingsPackageName = SETTINGS_APP_PACKAGE_NAME),
            getAppFunctionAgentUsageDetailsUseCase,
            savedStateHandle,
            backgroundScope,
            StandardTestDispatcher(testScheduler),
        )
    }

    private fun isTv(): Boolean =
        instrumentationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    private fun isAutomotive(): Boolean =
        instrumentationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

    private fun isWatch(): Boolean =
        instrumentationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)

    companion object {
        const val AGENT_NAME_1 = "agent1"
        const val AGENT_NAME_2 = "agent2"
        const val TARGET_NAME_1 = "target1"
        const val TARGET_NAME_2 = "target2"
        const val TARGET_NAME_3 = "target3"
        const val INTERACTION_URI_1 = "uri1"
        const val INTERACTION_URI_2 = "uri2"
        const val INTERACTION_URI_3 = "uri3"
        const val FLAG_ENABLE_APP_INTERACTION_API =
            "com.android.permissioncontroller.jarjar.${android.app.appfunctions.flags.Flags.FLAG_ENABLE_APP_INTERACTION_API}"
        const val SETTINGS_APP_PACKAGE_NAME = "com.google.android.settings.intelligence"
    }
}
