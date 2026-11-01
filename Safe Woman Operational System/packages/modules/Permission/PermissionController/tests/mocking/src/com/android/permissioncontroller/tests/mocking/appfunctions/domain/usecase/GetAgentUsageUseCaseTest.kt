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
package com.android.permissioncontroller.tests.mocking.appfunctions.domain.usecase

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.platform.test.annotations.RequiresFlagsDisabled
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.CheckFlagsRule
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAgentUsageUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.v37.GetAgentUsageUseCaseImpl
import com.android.permissioncontroller.appinteraction.domain.model.v31.AgentActivityItem
import com.android.permissioncontroller.appinteraction.domain.model.v37.AccessHistory
import com.android.permissioncontroller.flags.Flags
import com.android.permissioncontroller.tests.mocking.appinteraction.data.repository.FakeAppInteractionRepository
import com.android.permissioncontroller.tests.mocking.pm.data.repository.FakePackageRepository
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations

/** Unit tests for [GetAgentUsageUseCaseImpl]. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.BAKLAVA)
class GetAgentUsageUseCaseTest {
    @get:Rule val checkFlagsRule: CheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()
    @Mock private lateinit var mockContext: Context
    @Mock private lateinit var packageManager: PackageManager

    private lateinit var useCase: GetAgentUsageUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(mockContext.packageManager).thenReturn(packageManager)
        whenever(packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)).thenReturn(false)
        whenever(packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)).thenReturn(false)
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun getAgentUsages_success() = runTest {
        assumeTrue(
            "Skipping: Feature not supported on Auto when flag is disabled",
            !isAutomotive() || Flags.automotivePrivacyDashboardAgentActivityEnabled(),
        )
        val now = System.currentTimeMillis()
        val accessHistory =
            listOf(
                // Agent 1: 1 access in last 24 hours, 2 in last 7 days
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.HOURS.toMillis(1),
                ),
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_2,
                    accessTime = now - TimeUnit.DAYS.toMillis(3),
                ),
                // Agent 2: 0 accesses in last 24 hours, 1 in last 7 days
                createAccessHistory(
                    agentPackageName = AGENT_NAME_2,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.DAYS.toMillis(3),
                ),
                // Agent 3: 0 accesses in last 24 hours, 0 in last 7 days
                createAccessHistory(
                    agentPackageName = AGENT_NAME_3,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.DAYS.toMillis(8),
                ),
            )

        val agents = listOf(AGENT_NAME_1)
        val appInteractionRepository = FakeAppInteractionRepository(accessHistory)
        val packageRepository = FakePackageRepository(agents = agents)
        useCase = GetAgentUsageUseCaseImpl(appInteractionRepository, packageRepository)

        val result = useCase(mockContext)
        assertThat(result)
            .containsExactly(
                AgentActivityItem(AGENT_NAME_1, USER_0, 1, 2),
                AgentActivityItem(AGENT_NAME_2, USER_0, 0, 1),
                AgentActivityItem(AGENT_NAME_3, USER_0, 0, 0),
            )
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun getAgentUsages_resultIsDistinct() = runTest {
        assumeTrue(
            "Skipping: Feature not supported on Auto when flag is disabled",
            !isAutomotive() || Flags.automotivePrivacyDashboardAgentActivityEnabled(),
        )
        val now = System.currentTimeMillis()
        val accessHistory =
            listOf(
                // Agent 1: 2 accesses in last 24 hours, 2 in last 7 days
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.HOURS.toMillis(1),
                ),
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_2,
                    accessTime = now - TimeUnit.HOURS.toMillis(2),
                ),
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_2,
                    accessTime = now - TimeUnit.HOURS.toMillis(3),
                ),
            )
        val agents = listOf(AGENT_NAME_1)
        val appInteractionRepository = FakeAppInteractionRepository(accessHistory)
        val packageRepository = FakePackageRepository(agents = agents)
        useCase = GetAgentUsageUseCaseImpl(appInteractionRepository, packageRepository)

        val result = useCase(mockContext)
        assertThat(result).containsExactly(AgentActivityItem(AGENT_NAME_1, USER_0, 2, 2))
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun getAgentUsages_deviceAssistanceAccessesAreGrouped() = runTest {
        assumeTrue(
            "Skipping: Feature not supported on Auto when flag is disabled",
            !isAutomotive() || Flags.automotivePrivacyDashboardAgentActivityEnabled(),
        )
        val now = System.currentTimeMillis()
        val accessHistory =
            listOf(
                // Agent 1: 1 normal app function access, 2 device assistance accesses in last 24
                // hours
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.HOURS.toMillis(1),
                ),
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_2,
                    accessTime = now - TimeUnit.HOURS.toMillis(2),
                ),
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_3,
                    accessTime = now - TimeUnit.HOURS.toMillis(3),
                ),
            )
        val agents = listOf(AGENT_NAME_1)
        val deviceAssistancePackageNames = listOf(TARGET_NAME_2, TARGET_NAME_3)
        val appInteractionRepository =
            FakeAppInteractionRepository(accessHistory, deviceAssistancePackageNames)
        val packageRepository = FakePackageRepository(agents = agents)
        useCase = GetAgentUsageUseCaseImpl(appInteractionRepository, packageRepository)

        val result = useCase(mockContext)
        assertThat(result).containsExactly(AgentActivityItem(AGENT_NAME_1, USER_0, 2, 2))
    }

    @Test
    @RequiresFlagsDisabled(Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED)
    fun getAgentUsages_flagOff_emptyResult() = runTest {
        assumeTrue(
            "Skipping: skip test if feature is enabled and on automotive",
            !isAutomotive() || !Flags.automotivePrivacyDashboardAgentActivityEnabled(),
        )
        val now = System.currentTimeMillis()
        val accessHistory =
            listOf(
                // Agent 1: 1 accesses in last 24 hours, 1 in last 7 days
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.HOURS.toMillis(1),
                )
            )
        val agents = listOf(AGENT_NAME_1)
        val appInteractionRepository = FakeAppInteractionRepository(accessHistory)
        val packageRepository = FakePackageRepository(agents = agents)
        useCase = GetAgentUsageUseCaseImpl(appInteractionRepository, packageRepository)

        val result = useCase(mockContext)
        assertThat(result).hasSize(0)
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun getAgentUsages_shellAgentWithNoActivity_isExcluded() = runTest {
        assumeTrue(
            "Skipping: Feature not supported on Auto when flag is disabled",
            !isAutomotive() || Flags.automotivePrivacyDashboardAgentActivityEnabled(),
        )
        val now = System.currentTimeMillis()
        val accessHistory =
            listOf(
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.HOURS.toMillis(1),
                )
            )
        // com.android.shell holds the permission but has no access history
        val agents = listOf(AGENT_NAME_1, SHELL_PACKAGE_NAME)
        val appInteractionRepository = FakeAppInteractionRepository(accessHistory)
        val packageRepository = FakePackageRepository(agents = agents)
        useCase = GetAgentUsageUseCaseImpl(appInteractionRepository, packageRepository)

        val result = useCase(mockContext)

        // Verify that the normal agent is present, but the shell agent is excluded because it has
        // no activity.
        assertThat(result).containsExactly(AgentActivityItem(AGENT_NAME_1, USER_0, 1, 1))
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun getAgentUsages_shellAgentWithActivity_isIncluded() = runTest {
        assumeTrue(
            "Skipping: Feature not supported on Auto when flag is disabled",
            !isAutomotive() || Flags.automotivePrivacyDashboardAgentActivityEnabled(),
        )
        val now = System.currentTimeMillis()
        val accessHistory =
            listOf(
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.HOURS.toMillis(1),
                ),
                // com.android.shell has access history
                createAccessHistory(
                    agentPackageName = SHELL_PACKAGE_NAME,
                    targetPackageName = TARGET_NAME_2,
                    accessTime = now - TimeUnit.HOURS.toMillis(2),
                ),
            )
        // Both agents hold the permission
        val agents = listOf(AGENT_NAME_1, SHELL_PACKAGE_NAME)
        val appInteractionRepository = FakeAppInteractionRepository(accessHistory)
        val packageRepository = FakePackageRepository(agents = agents)
        useCase = GetAgentUsageUseCaseImpl(appInteractionRepository, packageRepository)

        val result = useCase(mockContext)

        // Verify that both agents are present in the result, including shell.
        assertThat(result)
            .containsExactly(
                AgentActivityItem(AGENT_NAME_1, USER_0, 1, 1),
                AgentActivityItem(SHELL_PACKAGE_NAME, USER_0, 1, 1),
            )
    }

    private fun createAccessHistory(
        agentPackageName: String,
        targetPackageName: String,
        accessTime: Long,
    ) = AccessHistory(agentPackageName, targetPackageName, null, null, null, accessTime)

    private fun isAutomotive(): Boolean {
        val testContext: Context = ApplicationProvider.getApplicationContext()
        return testContext.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
    }

    companion object {
        const val AGENT_NAME_1 = "agent1"
        const val AGENT_NAME_2 = "agent2"
        const val AGENT_NAME_3 = "agent3"
        const val SHELL_PACKAGE_NAME = "com.android.shell"
        const val TARGET_NAME_1 = "target1"
        const val TARGET_NAME_2 = "target2"
        const val TARGET_NAME_3 = "target3"
        const val USER_ID_0 = 0
        val USER_0 = UserHandle.of(USER_ID_0)

        const val FLAG_ENABLE_APP_INTERACTION_API =
            "com.android.permissioncontroller.jarjar.${android.app.appfunctions.flags.Flags.FLAG_ENABLE_APP_INTERACTION_API}"
    }
}
