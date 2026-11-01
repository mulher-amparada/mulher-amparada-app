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

import android.os.Build
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.appfunctions.data.repository.AppFunctionRepository
import com.android.permissioncontroller.appfunctions.ui.viewmodel.AgentListUiState
import com.android.permissioncontroller.appfunctions.ui.viewmodel.AgentListViewModel
import com.android.permissioncontroller.common.model.Stateful
import com.android.permissioncontroller.tests.mocking.appfunctions.data.repository.FakeAppFunctionRepository
import com.android.permissioncontroller.tests.mocking.coroutines.collectLastValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import org.mockito.MockitoSession
import org.mockito.quality.Strictness

// TODO(b/424004217): Update this to the correct version code
/**
 * These unit tests are for app function agent list implementation, the view model class is
 * [AgentListViewModel]
 */
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
@RequiresFlagsEnabled(
    AgentListViewModelTest.FLAG_APP_FUNCTION_ACCESS_API_ENABLED,
    AgentListViewModelTest.FLAG_APP_FUNCTION_ACCESS_UI_ENABLED,
)
@RunWith(AndroidJUnit4::class)
class AgentListViewModelTest {
    @get:Rule val checkFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    @Mock private lateinit var application: PermissionControllerApplication
    private var mockitoSession: MockitoSession? = null

    private lateinit var appFunctionRepository: AppFunctionRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockitoSession =
            ExtendedMockito.mockitoSession()
                .mockStatic(PermissionControllerApplication::class.java)
                .strictness(Strictness.LENIENT)
                .startMocking()
        ExtendedMockito.doReturn(application).`when` { PermissionControllerApplication.get() }
        whenever(application.applicationContext).thenReturn(application)
        whenever(application.registerReceiverForAllUsers(any(), any(), any(), any()))
            .thenReturn(null)

        appFunctionRepository = FakeAppFunctionRepository(agents = agentPackageNames)
    }

    @After
    fun finish() {
        mockitoSession?.finishMocking()
    }

    @Test
    fun allAgentsShown() = runTest {
        val expectedAgents = agentPackageNames

        val viewModel = getViewModel()
        val uiState = getAgentListUiState(viewModel)

        assertTrue(uiState is Stateful.Success)
        assertThat(uiState.value!!.agentPackageNames).containsExactlyElementsIn(expectedAgents)
    }

    private fun TestScope.getViewModel(): AgentListViewModel {
        return AgentListViewModel(
            application,
            appFunctionRepository,
            backgroundScope,
            StandardTestDispatcher(testScheduler),
        )
    }

    private fun TestScope.getAgentListUiState(
        viewModel: AgentListViewModel
    ): Stateful<AgentListUiState> {
        val result by collectLastValue(viewModel.uiStateFlow)
        return result!!
    }

    companion object {
        // Flag lib changes has caused issues with jarjar and now annotations require the jarjar
        // package prepended to the flag string
        const val FLAG_APP_FUNCTION_ACCESS_API_ENABLED =
            "com.android.permissioncontroller.jarjar.android.permission.flags.app_function_access_api_enabled"
        const val FLAG_APP_FUNCTION_ACCESS_UI_ENABLED =
            "com.android.permissioncontroller.jarjar.android.permission.flags.app_function_access_ui_enabled"

        private const val TEST_AGENT_PACKAGE_NAME = "test.agent.package"
        private const val TEST_AGENT_PACKAGE_NAME2 = "test.agent.package2"
        private val agentPackageNames = listOf(TEST_AGENT_PACKAGE_NAME, TEST_AGENT_PACKAGE_NAME2)
    }
}
