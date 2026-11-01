/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.permissioncontroller.tests.mocking.permission.ui.model

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.platform.test.annotations.RequiresFlagsDisabled
import android.platform.test.annotations.RequiresFlagsEnabled
import android.platform.test.flag.junit.CheckFlagsRule
import android.platform.test.flag.junit.DeviceFlagsValueProvider
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.android.dx.mockito.inline.extended.ExtendedMockito
import com.android.modules.utils.build.SdkLevel
import com.android.permissioncontroller.DeviceUtils
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.appfunctions.domain.usecase.GetAppFunctionPackageInfoUseCaseImpl
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAgentUsageUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAppFunctionPackageInfoUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.v37.GetAgentUsageUseCaseImpl
import com.android.permissioncontroller.appinteraction.domain.model.v37.AccessHistory
import com.android.permissioncontroller.appops.data.model.v31.PackageAppOpUsageModel
import com.android.permissioncontroller.appops.data.model.v31.PackageAppOpUsageModel.AppOpUsageModel
import com.android.permissioncontroller.flags.Flags
import com.android.permissioncontroller.permission.data.repository.v31.PermissionRepository
import com.android.permissioncontroller.permission.domain.usecase.v31.GetPermissionGroupUsageUseCase
import com.android.permissioncontroller.permission.ui.viewmodel.v31.PermissionUsageViewModel
import com.android.permissioncontroller.permission.ui.viewmodel.v31.PermissionUsagesUiState
import com.android.permissioncontroller.permission.utils.PermissionMapping
import com.android.permissioncontroller.pm.data.model.v31.PackageInfoModel
import com.android.permissioncontroller.tests.mocking.appinteraction.data.repository.FakeAppInteractionRepository
import com.android.permissioncontroller.tests.mocking.appops.data.repository.FakeAppOpRepository
import com.android.permissioncontroller.tests.mocking.coroutines.collectLastValue
import com.android.permissioncontroller.tests.mocking.permission.data.repository.FakePermissionRepository
import com.android.permissioncontroller.tests.mocking.pm.data.repository.FakePackageRepository
import com.android.permissioncontroller.tests.mocking.role.data.repository.FakeRoleRepository
import com.android.permissioncontroller.tests.mocking.user.data.repository.FakeUserRepository
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import org.mockito.MockitoSession
import org.mockito.quality.Strictness

@RunWith(AndroidJUnit4::class)
class PermissionUsageViewModelTest {
    @get:Rule val checkFlagsRule: CheckFlagsRule = DeviceFlagsValueProvider.createCheckFlagsRule()

    @Mock private lateinit var application: PermissionControllerApplication
    @Mock private lateinit var context: Context
    @Mock private lateinit var packageManager: PackageManager
    @Mock private lateinit var userManager: UserManager
    @Mock private lateinit var userHandle: UserHandle
    private var mockitoSession: MockitoSession? = null

    private lateinit var permissionRepository: PermissionRepository
    private val currentUser = android.os.Process.myUserHandle()
    private val testPackageName = "test.package"
    private val systemPackageName = "test.package.system"
    private lateinit var packageInfos: MutableMap<String, PackageInfoModel>

    @Before
    fun setup() {
        assumeTrue(SdkLevel.isAtLeastS())
        MockitoAnnotations.initMocks(this)
        mockitoSession =
            ExtendedMockito.mockitoSession()
                .mockStatic(PermissionControllerApplication::class.java)
                .mockStatic(DeviceUtils::class.java)
                .strictness(Strictness.LENIENT)
                .startMocking()

        whenever(PermissionControllerApplication.get()).thenReturn(application)
        whenever(application.applicationContext).thenReturn(context)
        whenever(DeviceUtils.isHandheld()).thenReturn(true)
        whenever(context.packageManager).thenReturn(packageManager)
        whenever(packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)).thenReturn(false)
        whenever(packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)).thenReturn(false)
        whenever(context.getSystemService(UserManager::class.java)).thenReturn(userManager)
        whenever(userManager.userProfiles).thenReturn(listOf(userHandle))
        PermissionMapping.addHealthPermissionsToPlatform(setOf("health1"))

        val permissionFlags =
            mapOf<String, Int>(
                CAMERA_PERMISSION to PackageManager.FLAG_PERMISSION_USER_SENSITIVE_WHEN_GRANTED,
                RECORD_AUDIO_PERMISSION to 0, // not user sensitive
            )
        permissionRepository = FakePermissionRepository(permissionFlags)
        packageInfos =
            mapOf(
                    testPackageName to getPackageInfoModel(testPackageName),
                    systemPackageName to
                        getPackageInfoModel(
                            systemPackageName,
                            applicationFlags = ApplicationInfo.FLAG_SYSTEM,
                        ),
                )
                .toMutableMap()
    }

    @After
    fun finish() {
        mockitoSession?.finishMocking()
    }

    @Test
    fun allPermissionGroupsAreShown() = runTest {
        val permissionUsageViewModel = getViewModel()
        val uiData = getPermissionUsageUiState(permissionUsageViewModel)

        val expectedPermissions = PermissionMapping.getPlatformPermissionGroups().toMutableSet()
        if (SdkLevel.isAtLeastT()) {
            expectedPermissions.remove(android.Manifest.permission_group.NOTIFICATIONS)
        }
        assertThat(uiData.permissionGroupUsageCount.keys).isEqualTo(expectedPermissions)
    }

    @Test
    fun onlyNonSystemAppsUsageIsCounted() = runTest {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5)
        val appOpsUsage =
            listOf(
                AppOpUsageModel(AppOpsManager.OPSTR_CAMERA, timestamp),
                AppOpUsageModel(AppOpsManager.OPSTR_PHONE_CALL_MICROPHONE, timestamp),
            )
        val appOpsUsageModels =
            listOf(
                PackageAppOpUsageModel(testPackageName, appOpsUsage, currentUser.identifier),
                PackageAppOpUsageModel(systemPackageName, appOpsUsage, currentUser.identifier),
            )
        val permissionUsageUseCase = getPermissionGroupUsageUseCase(appOpsUsageModels)
        val permissionUsageViewModel =
            getViewModel(
                permissionUsageUseCase = permissionUsageUseCase,
                savedStateHandle = SavedStateHandle(mapOf("showSystem" to false)),
            )
        val uiData = getPermissionUsageUiState(permissionUsageViewModel)

        val permissionGroupsCount = uiData.permissionGroupUsageCount
        assertThat(permissionGroupsCount[CAMERA_PERMISSION_GROUP]).isEqualTo(2)
        assertThat(permissionGroupsCount[MICROPHONE_PERMISSION_GROUP]).isEqualTo(1)
    }

    @Test
    fun systemAppsUsageIsCounted() = runTest {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5)
        val appOpsUsage =
            listOf(
                AppOpUsageModel(AppOpsManager.OPSTR_CAMERA, timestamp),
                AppOpUsageModel(AppOpsManager.OPSTR_PHONE_CALL_MICROPHONE, timestamp),
            )
        val appOpsUsageModels =
            listOf(
                PackageAppOpUsageModel(testPackageName, appOpsUsage, currentUser.identifier),
                PackageAppOpUsageModel(systemPackageName, appOpsUsage, currentUser.identifier),
            )
        val permissionUsageUseCase = getPermissionGroupUsageUseCase(appOpsUsageModels)
        val permissionUsageViewModel =
            getViewModel(
                permissionUsageUseCase = permissionUsageUseCase,
                savedStateHandle = SavedStateHandle(mapOf("showSystem" to true)),
            )
        val uiData = getPermissionUsageUiState(permissionUsageViewModel)

        val permissionGroupsCount = uiData.permissionGroupUsageCount
        assertThat(permissionGroupsCount[CAMERA_PERMISSION_GROUP]).isEqualTo(2)
        assertThat(permissionGroupsCount[MICROPHONE_PERMISSION_GROUP]).isEqualTo(2)
    }

    @Test
    fun noSystemAppsAvailableInLast24Hours() = runTest {
        val timestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
        val appOpsUsage =
            listOf(
                AppOpUsageModel(AppOpsManager.OPSTR_CAMERA, timestamp),
                AppOpUsageModel(AppOpsManager.OPSTR_PHONE_CALL_MICROPHONE, timestamp),
            )
        val appOpsUsageModels =
            listOf(
                PackageAppOpUsageModel(testPackageName, appOpsUsage, currentUser.identifier),
                PackageAppOpUsageModel(systemPackageName, appOpsUsage, currentUser.identifier),
            )
        val permissionUsageUseCase = getPermissionGroupUsageUseCase(appOpsUsageModels)
        val permissionUsageViewModel = getViewModel(permissionUsageUseCase = permissionUsageUseCase)
        val uiData = getPermissionUsageUiState(permissionUsageViewModel)

        assertThat(uiData.containsSystemAppUsage).isFalse()
    }

    @Test
    fun appUsageIsCountedForLast7Days() = runTest {
        val timestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
        val appOpsUsage =
            listOf(
                AppOpUsageModel(AppOpsManager.OPSTR_CAMERA, timestamp),
                AppOpUsageModel(AppOpsManager.OPSTR_PHONE_CALL_MICROPHONE, timestamp),
            )
        val appOpsUsageModels =
            listOf(PackageAppOpUsageModel(testPackageName, appOpsUsage, currentUser.identifier))
        val permissionUsageUseCase = getPermissionGroupUsageUseCase(appOpsUsageModels)
        val permissionUsageViewModel =
            getViewModel(
                permissionUsageUseCase = permissionUsageUseCase,
                savedStateHandle = SavedStateHandle(mapOf("show7Days" to true)),
            )
        val permissionGroupsCount =
            getPermissionUsageUiState(permissionUsageViewModel).permissionGroupUsageCount

        assertThat(permissionGroupsCount[CAMERA_PERMISSION_GROUP]).isEqualTo(1)
        assertThat(permissionGroupsCount[MICROPHONE_PERMISSION_GROUP]).isEqualTo(1)
    }

    @Test
    fun verifyObserverIsNotifiedOnUserActionWhenDataIsSame() = runTest {
        val timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)
        val appOpsUsage =
            listOf(
                AppOpUsageModel(AppOpsManager.OPSTR_CAMERA, timestamp),
                AppOpUsageModel(AppOpsManager.OPSTR_PHONE_CALL_MICROPHONE, timestamp),
            )
        val appOpsUsageModels =
            listOf(PackageAppOpUsageModel(testPackageName, appOpsUsage, currentUser.identifier))
        val permissionUsageUseCase = getPermissionGroupUsageUseCase(appOpsUsageModels)
        val permissionUsageViewModel =
            getViewModel(
                permissionUsageUseCase = permissionUsageUseCase,
                savedStateHandle = SavedStateHandle(mapOf("show7Days" to false)),
            )

        val uiState = getPermissionUsageUiState(permissionUsageViewModel)
        assertThat(uiState.show7Days).isFalse()

        // perform user action
        permissionUsageViewModel.updateShow7Days(true)
        val uiState2 = getPermissionUsageUiState(permissionUsageViewModel)
        assertThat(uiState2.show7Days).isTrue()
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun verifyAgentUsagesAreShownForPast24Hours() = runTest {
        assumeFalse(isTv() || isWatch())
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
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_2,
                    accessTime = now - TimeUnit.HOURS.toMillis(1),
                ),
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_3,
                    accessTime = now - TimeUnit.DAYS.toMillis(3),
                ),
                createAccessHistory(
                    agentPackageName = AGENT_NAME_2,
                    targetPackageName = TARGET_NAME_1,
                    accessTime = now - TimeUnit.HOURS.toMillis(3),
                ),
            )
        val agents = listOf(AGENT_NAME_1, AGENT_NAME_2)
        val permissionUsageUseCase = getPermissionGroupUsageUseCase()
        val appFunctionPackageInfoUseCase = getAppFunctionPackageInfoUseCase()
        val appFunctionAgentUsageUseCase = getAppFunctionAgentUsageUseCase(accessHistory, agents)
        val permissionUsageViewModel =
            getViewModel(
                permissionUsageUseCase = permissionUsageUseCase,
                appFunctionAgentUsageUseCase = appFunctionAgentUsageUseCase,
                appFunctionPackageInfoUseCase = appFunctionPackageInfoUseCase,
                savedStateHandle = SavedStateHandle(mapOf("show7Days" to false)),
            )
        val agentUsages = getPermissionUsageUiState(permissionUsageViewModel).agentUsages
        assertThat(agentUsages).hasSize(2)
        assertThat(agentUsages[0].agentPackageName).isEqualTo(AGENT_NAME_1)
        assertThat(agentUsages[0].accessCount24Hours).isEqualTo(2)
        assertThat(agentUsages[1].agentPackageName).isEqualTo(AGENT_NAME_2)
        assertThat(agentUsages[1].accessCount24Hours).isEqualTo(1)
    }

    @Test
    @RequiresFlagsEnabled(
        Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED,
        FLAG_ENABLE_APP_INTERACTION_API,
    )
    fun verifyAgentUsagesAreShownForPast7Days() = runTest {
        assumeFalse(isTv() || isWatch())
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
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_2,
                    accessTime = now - TimeUnit.DAYS.toMillis(3),
                ),
                createAccessHistory(
                    agentPackageName = AGENT_NAME_1,
                    targetPackageName = TARGET_NAME_3,
                    accessTime = now - TimeUnit.DAYS.toMillis(10),
                ),
            )
        val agents = listOf(AGENT_NAME_1)
        val permissionUsageUseCase = getPermissionGroupUsageUseCase()
        val appFunctionPackageInfoUseCase = getAppFunctionPackageInfoUseCase()
        val appFunctionAgentUsageUseCase = getAppFunctionAgentUsageUseCase(accessHistory, agents)
        val permissionUsageViewModel =
            getViewModel(
                permissionUsageUseCase = permissionUsageUseCase,
                appFunctionAgentUsageUseCase = appFunctionAgentUsageUseCase,
                appFunctionPackageInfoUseCase = appFunctionPackageInfoUseCase,
                savedStateHandle = SavedStateHandle(mapOf("show7Days" to true)),
            )
        val agentUsages = getPermissionUsageUiState(permissionUsageViewModel).agentUsages
        assertThat(agentUsages).hasSize(1)
        assertThat(agentUsages[0].accessCount7Days).isEqualTo(2)
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.BAKLAVA)
    @RequiresFlagsDisabled(Flags.FLAG_PRIVACY_DASHBOARD_AGENT_ACTIVITY_ENABLED)
    fun verifyNoAgentUsagesBelowC() = runTest {
        assumeFalse(isTv() || isWatch())
        assumeTrue(
            "Skipping: skip test if feature is enabled and on automotive",
            !isAutomotive() || !Flags.automotivePrivacyDashboardAgentActivityEnabled(),
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
        val agents = listOf(AGENT_NAME_1)
        val permissionUsageUseCase = getPermissionGroupUsageUseCase()
        val appFunctionPackageInfoUseCase = getAppFunctionPackageInfoUseCase()
        val appFunctionAgentUsageUseCase = getAppFunctionAgentUsageUseCase(accessHistory, agents)
        val permissionUsageViewModel =
            getViewModel(
                permissionUsageUseCase = permissionUsageUseCase,
                appFunctionAgentUsageUseCase = appFunctionAgentUsageUseCase,
                appFunctionPackageInfoUseCase = appFunctionPackageInfoUseCase,
                savedStateHandle = SavedStateHandle(mapOf("show7Days" to false)),
            )
        val agentUsages = getPermissionUsageUiState(permissionUsageViewModel).agentUsages
        // We should be using NoOpAgentUsageUseCase. Hence no agent usages should be returned.
        assertThat(agentUsages).hasSize(0)
    }

    private fun TestScope.getViewModel(
        permissionUsageUseCase: GetPermissionGroupUsageUseCase = getPermissionGroupUsageUseCase(),
        appFunctionAgentUsageUseCase: GetAgentUsageUseCase = getAppFunctionAgentUsageUseCase(),
        appFunctionPackageInfoUseCase: GetAppFunctionPackageInfoUseCase =
            getAppFunctionPackageInfoUseCase(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(emptyMap()),
    ): PermissionUsageViewModel {
        return PermissionUsageViewModel(
            application,
            permissionRepository,
            permissionUsageUseCase,
            appFunctionAgentUsageUseCase,
            appFunctionPackageInfoUseCase,
            backgroundScope,
            StandardTestDispatcher(testScheduler),
            savedState = savedStateHandle,
        )
    }

    private fun TestScope.getPermissionUsageUiState(
        viewModel: PermissionUsageViewModel
    ): PermissionUsagesUiState.Success {
        val result by collectLastValue(viewModel.permissionUsagesUiDataFlow)
        return result as PermissionUsagesUiState.Success
    }

    private fun createAccessHistory(
        agentPackageName: String,
        targetPackageName: String,
        accessTime: Long,
    ) = AccessHistory(agentPackageName, targetPackageName, null, null, null, accessTime)

    private fun getPermissionGroupUsageUseCase(
        packageAppOpsUsages: List<PackageAppOpUsageModel> = emptyList()
    ): GetPermissionGroupUsageUseCase {
        val userRepository = FakeUserRepository(listOf(currentUser.identifier))
        val roleRepository = FakeRoleRepository()
        val packageRepository = FakePackageRepository(packageInfos)
        val appOpUsageRepository = FakeAppOpRepository(flowOf(packageAppOpsUsages))
        return GetPermissionGroupUsageUseCase(
            packageRepository,
            permissionRepository,
            appOpUsageRepository,
            roleRepository,
            userRepository,
        )
    }

    private fun getAppFunctionAgentUsageUseCase(
        accessHistory: List<AccessHistory> = emptyList(),
        agents: List<String> = emptyList(),
    ): GetAgentUsageUseCase {
        val appInteractionRepository = FakeAppInteractionRepository(accessHistory)
        val packageRepository = FakePackageRepository(agents = agents)
        return GetAgentUsageUseCaseImpl(appInteractionRepository, packageRepository)
    }

    private fun getAppFunctionPackageInfoUseCase(): GetAppFunctionPackageInfoUseCase {
        val packageRepository = FakePackageRepository(packageInfos)
        return GetAppFunctionPackageInfoUseCaseImpl(packageRepository)
    }

    private fun getPackageInfoModel(
        packageName: String,
        requestedPermissions: List<String> = listOf(CAMERA_PERMISSION, RECORD_AUDIO_PERMISSION),
        permissionsFlags: List<Int> =
            listOf(
                PackageInfo.REQUESTED_PERMISSION_GRANTED,
                PackageInfo.REQUESTED_PERMISSION_GRANTED,
            ),
        applicationFlags: Int = 0,
    ) = PackageInfoModel(packageName, requestedPermissions, permissionsFlags, applicationFlags)

    private fun isTv(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    private fun isAutomotive(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

    private fun isWatch(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)

    companion object {
        private val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
        private val RECORD_AUDIO_PERMISSION = android.Manifest.permission.RECORD_AUDIO
        private val CAMERA_PERMISSION_GROUP = android.Manifest.permission_group.CAMERA
        private val MICROPHONE_PERMISSION_GROUP = android.Manifest.permission_group.MICROPHONE
        const val AGENT_NAME_1 = "agent1"
        const val AGENT_NAME_2 = "agent2"
        const val TARGET_NAME_1 = "target1"
        const val TARGET_NAME_2 = "target2"
        const val TARGET_NAME_3 = "target3"
        const val ACCESS_DURATION = 1000L

        // Flag lib changes has caused issues with jarjar and now annotations require the jarjar
        // package prepended to the flag string
        const val FLAG_ENABLE_APP_INTERACTION_API =
            "com.android.permissioncontroller.jarjar.${android.app.appfunctions.flags.Flags.FLAG_ENABLE_APP_INTERACTION_API}"
    }
}
