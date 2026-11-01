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

import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_DENIED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_GRANTED
import android.os.Build
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.appfunctions.data.repository.AppFunctionRepository
import com.android.permissioncontroller.appfunctions.domain.usecase.GetAccessRequestStateUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.UpdateAccessUseCase
import com.android.permissioncontroller.appfunctions.ui.viewmodel.AgentAccessUiState
import com.android.permissioncontroller.appfunctions.ui.viewmodel.AgentAccessViewModel
import com.android.permissioncontroller.common.model.Stateful
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository
import com.android.permissioncontroller.tests.mocking.appfunctions.data.repository.FakeAppFunctionRepository
import com.android.permissioncontroller.tests.mocking.coroutines.collectLastValue
import com.android.permissioncontroller.tests.mocking.pm.data.repository.FakePackageRepository
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.Executor
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
import org.mockito.Mockito
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import org.mockito.MockitoSession
import org.mockito.quality.Strictness

// TODO(b/424004217): Update this to the correct version code
/**
 * These unit tests are for app function agent access implementation, the view model class is
 * [AgentAccessViewModel]
 */
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
@RequiresFlagsEnabled(
    AgentAccessViewModelTest.FLAG_APP_FUNCTION_ACCESS_API_ENABLED,
    AgentAccessViewModelTest.FLAG_APP_FUNCTION_ACCESS_UI_ENABLED,
)
@RunWith(AndroidJUnit4::class)
class AgentAccessViewModelTest {
    @get:Rule val checkFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    @Mock private lateinit var application: PermissionControllerApplication

    private lateinit var appFunctionRepository: AppFunctionRepository
    private lateinit var packageRepository: PackageRepository

    private var mockitoSession: MockitoSession? = null

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
        whenever(application.mainExecutor).thenReturn(Mockito.mock(Executor::class.java))
        whenever(application.registerReceiverForAllUsers(any(), any(), any(), any()))
            .thenReturn(null)

        appFunctionRepository =
            FakeAppFunctionRepository(
                agents = agentPackageNames,
                targets = targetPackageNames,
                accessFlags = accessFlags,
            )
        packageRepository = FakePackageRepository()
    }

    @After
    fun finish() {
        mockitoSession?.finishMocking()
    }

    @Test
    fun validAgentShown_emptyTargets() = runTest {
        val testAgent = TEST_AGENT_PACKAGE_NAME

        // Set all appFunctionRepository with no accessFlags defined
        appFunctionRepository =
            FakeAppFunctionRepository(agents = agentPackageNames, targets = targetPackageNames)

        val viewModel = getViewModel(testAgent)
        val uiState = getAgentAccessUiState(viewModel)

        assertTrue(uiState is Stateful.Success)
        // Correct Agent returned
        assertThat(uiState.value!!.agentPackageName).isEqualTo(testAgent)

        // Correct Targets returned
        assertThat(uiState.value!!.allowedTargetPackageNames).isEmpty()
        assertThat(uiState.value!!.notAllowedTargetPackageNames).isEmpty()
    }

    @Test
    fun validAgentShown_expectedTargets() = runTest {
        val testAgent = TEST_AGENT_PACKAGE_NAME
        val expectedAccessStates =
            accessFlags
                .filter { it.key.first == testAgent }
                .filter {
                    it.value == ACCESS_FLAG_USER_GRANTED || it.value == ACCESS_FLAG_USER_DENIED
                }
                .mapValues { it.value == ACCESS_FLAG_USER_GRANTED }
        val expectedAllowedTargets =
            targetPackageNames
                .filter { expectedAccessStates.containsKey(testAgent to it) }
                .filter { expectedAccessStates[testAgent to it] == true }
        val expectedDeniedTargets =
            targetPackageNames
                .filter { expectedAccessStates.containsKey(testAgent to it) }
                .filter { expectedAccessStates[testAgent to it] == false }

        val viewModel = getViewModel(testAgent)
        val uiState = getAgentAccessUiState(viewModel)

        assertTrue(uiState is Stateful.Success)
        // Correct Agent returned
        assertThat(uiState.value!!.agentPackageName).isEqualTo(testAgent)

        // Correct Targets returned
        assertThat(uiState.value!!.allowedTargetPackageNames)
            .containsExactlyElementsIn(expectedAllowedTargets)
        assertThat(uiState.value!!.notAllowedTargetPackageNames)
            .containsExactlyElementsIn(expectedDeniedTargets)
    }

    @Test
    fun onAccessStateUpdated() = runTest {
        val testTarget = TEST_TARGET_PACKAGE_NAME
        val viewModel = getViewModel(TEST_AGENT_PACKAGE_NAME)
        val updateAccessUseCase = UpdateAccessUseCase(appFunctionRepository)

        updateAccessUseCase(TEST_AGENT_PACKAGE_NAME, DEVICE_SETTINGS_TARGET_PACKAGE_NAME, false)
        assertThat(
                getAgentAccessUiState(viewModel).value!!.allowedTargetPackageNames.filter {
                    it == testTarget
                }
            )
            .contains(testTarget)
        assertThat(
                getAgentAccessUiState(viewModel).value!!.notAllowedTargetPackageNames.filter {
                    it == testTarget
                }
            )
            .isEmpty()

        updateAccessUseCase(TEST_AGENT_PACKAGE_NAME, DEVICE_SETTINGS_TARGET_PACKAGE_NAME, true)
        assertThat(
                getAgentAccessUiState(viewModel).value!!.allowedTargetPackageNames.filter {
                    it == testTarget
                }
            )
            .contains(testTarget)
        assertThat(
                getAgentAccessUiState(viewModel).value!!.notAllowedTargetPackageNames.filter {
                    it == testTarget
                }
            )
            .isEmpty()
    }

    private fun TestScope.getViewModel(agentPackageName: String): AgentAccessViewModel {
        return AgentAccessViewModel(
            application,
            agentPackageName,
            appFunctionRepository,
            GetAccessRequestStateUseCase(appFunctionRepository),
            backgroundScope,
            StandardTestDispatcher(testScheduler),
        )
    }

    private fun TestScope.getAgentAccessUiState(
        viewModel: AgentAccessViewModel
    ): Stateful<AgentAccessUiState> {
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
        private const val DEVICE_SETTINGS_TARGET_PACKAGE_NAME = "android"
        private const val TEST_TARGET_PACKAGE_NAME = "test.target.package"
        private const val TEST_TARGET_PACKAGE_NAME2 = "test.target.package2"
        private const val TEST_TARGET_PACKAGE_NAME3 = "test.target.package3"
        private val agentPackageNames = listOf(TEST_AGENT_PACKAGE_NAME, TEST_AGENT_PACKAGE_NAME2)
        private val targetPackageNames =
            listOf(
                DEVICE_SETTINGS_TARGET_PACKAGE_NAME,
                TEST_TARGET_PACKAGE_NAME,
                TEST_TARGET_PACKAGE_NAME2,
                TEST_TARGET_PACKAGE_NAME3,
            )
        private val accessFlags =
            mapOf(
                (TEST_AGENT_PACKAGE_NAME to DEVICE_SETTINGS_TARGET_PACKAGE_NAME) to
                    ACCESS_FLAG_USER_GRANTED,
                (TEST_AGENT_PACKAGE_NAME to TEST_TARGET_PACKAGE_NAME) to ACCESS_FLAG_USER_GRANTED,
                (TEST_AGENT_PACKAGE_NAME to TEST_TARGET_PACKAGE_NAME2) to ACCESS_FLAG_USER_DENIED,
                (TEST_AGENT_PACKAGE_NAME to TEST_TARGET_PACKAGE_NAME3) to 0, // invalid
                (TEST_AGENT_PACKAGE_NAME2 to DEVICE_SETTINGS_TARGET_PACKAGE_NAME) to
                    ACCESS_FLAG_USER_GRANTED,
                (TEST_AGENT_PACKAGE_NAME2 to TEST_TARGET_PACKAGE_NAME) to ACCESS_FLAG_USER_DENIED,
                (TEST_AGENT_PACKAGE_NAME2 to TEST_TARGET_PACKAGE_NAME2) to ACCESS_FLAG_USER_GRANTED,
                (TEST_AGENT_PACKAGE_NAME2 to TEST_TARGET_PACKAGE_NAME3) to 0, // invalid
            )
    }
}
