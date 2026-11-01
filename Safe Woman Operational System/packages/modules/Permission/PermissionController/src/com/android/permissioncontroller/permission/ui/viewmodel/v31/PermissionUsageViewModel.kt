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

package com.android.permissioncontroller.permission.ui.viewmodel.v31

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.permissioncontroller.DeviceUtils
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil
import com.android.permissioncontroller.appfunctions.domain.usecase.GetAppFunctionPackageInfoUseCaseImpl
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAgentUsageUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAppFunctionPackageInfoUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.NoOpAgentUsageUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.NoOpAppFunctionPackageInfoUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.v37.GetAgentUsageUseCaseImpl
import com.android.permissioncontroller.appinteraction.data.repository.AppInteractionRepository
import com.android.permissioncontroller.appinteraction.domain.model.v31.AgentActivityItem
import com.android.permissioncontroller.common.model.Stateful
import com.android.permissioncontroller.permission.data.repository.v31.PermissionRepository
import com.android.permissioncontroller.permission.domain.model.v31.PermissionGroupUsageModel
import com.android.permissioncontroller.permission.domain.model.v31.PermissionGroupUsageModelWrapper
import com.android.permissioncontroller.permission.domain.usecase.v31.GetPermissionGroupUsageUseCase
import com.android.permissioncontroller.permission.ui.model.v31.PermissionUsageDetailsViewModel.Companion.SHOULD_SHOW_7_DAYS_KEY
import com.android.permissioncontroller.permission.ui.model.v31.PermissionUsageDetailsViewModel.Companion.SHOULD_SHOW_SYSTEM_KEY
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Privacy dashboard's new implementation. */
class PermissionUsageViewModel(
    val app: Application,
    private val permissionRepository: PermissionRepository,
    private val getPermissionUsageUseCase: GetPermissionGroupUsageUseCase,
    private val getAppFunctionAgentUsageUseCase: GetAgentUsageUseCase,
    private val getAppFunctionPackageInfoUseCase: GetAppFunctionPackageInfoUseCase,
    scope: CoroutineScope? = null,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val savedState: SavedStateHandle = SavedStateHandle(emptyMap()),
) : AndroidViewModel(app) {
    private val showSystemFlow = MutableStateFlow(savedState[SHOULD_SHOW_SYSTEM_KEY] ?: false)
    private val show7DaysFlow = MutableStateFlow(savedState[SHOULD_SHOW_7_DAYS_KEY] ?: false)
    private val coroutineScope = scope ?: viewModelScope

    private val permissionUsagesUiStateFlow: StateFlow<PermissionGroupUsageModelWrapper> by lazy {
        getPermissionUsageUseCase()
            .flowOn(defaultDispatcher)
            .stateIn(
                coroutineScope,
                SharingStarted.WhileSubscribed(5000),
                PermissionGroupUsageModelWrapper.Loading,
            )
    }

    private val agentUsageStateFlow =
        MutableStateFlow<Stateful<List<AgentActivityItem>>>(Stateful.Loading())

    private val agentUsageUiStateFlow: StateFlow<Stateful<List<AgentActivityItem>>> by lazy {
        coroutineScope.launch(defaultDispatcher) {
            val agentUsageUseCase = getAppFunctionAgentUsageUseCase(app.applicationContext)
            agentUsageStateFlow.value = Stateful.Success(agentUsageUseCase)
        }
        agentUsageStateFlow
    }

    @VisibleForTesting
    val permissionUsagesUiDataFlow: Flow<PermissionUsagesUiState> by lazy {
        combine(
                permissionUsagesUiStateFlow,
                agentUsageUiStateFlow,
                showSystemFlow,
                show7DaysFlow,
            ) { permGroupUsages, agentAccessCountState, showSystemApps, show7Days ->
                buildPermissionUsagesUiState(
                    permGroupUsages,
                    agentAccessCountState,
                    showSystemApps,
                    show7Days,
                )
            }
            .flowOn(defaultDispatcher)
    }

    val permissionUsagesUiLiveData =
        permissionUsagesUiDataFlow.asLiveData(context = coroutineScope.coroutineContext)

    /** Get start time based on whether to show 24 hours or 7 days data. */
    private fun getStartTime(show7DaysData: Boolean): Long {
        val curTime = System.currentTimeMillis()
        val showPermissionUsagesDuration =
            if (show7DaysData && DeviceUtils.isHandheld()) {
                TIME_7_DAYS_DURATION
            } else {
                TIME_24_HOURS_DURATION
            }
        return max(curTime - showPermissionUsagesDuration, Instant.EPOCH.toEpochMilli())
    }

    /** Builds a [PermissionUsagesUiState] containing all data necessary to render the UI. */
    private fun buildPermissionUsagesUiState(
        usages: PermissionGroupUsageModelWrapper,
        agentUsages: Stateful<List<AgentActivityItem>>,
        showSystemApps: Boolean,
        show7DaysData: Boolean,
    ): PermissionUsagesUiState {
        if (usages is PermissionGroupUsageModelWrapper.Loading || agentUsages is Stateful.Loading) {
            return PermissionUsagesUiState.Loading
        }

        if (agentUsages is Stateful.Failure) {
            return PermissionUsagesUiState.Failure
        }

        val permissionGroupOps: List<PermissionGroupUsageModel> =
            (usages as PermissionGroupUsageModelWrapper.Success).permissionUsageModels

        val startTime = getStartTime(show7DaysData)
        val dashboardPermissionGroups =
            permissionRepository.getPermissionGroupsForPrivacyDashboard()
        val permissionUsageCountMap = HashMap<String, Int>(dashboardPermissionGroups.size)
        for (permissionGroup in dashboardPermissionGroups) {
            permissionUsageCountMap[permissionGroup] = 0
        }

        val permGroupOps = permissionGroupOps.filter { it.lastAccessTimestampMillis > startTime }
        permGroupOps
            .filter { showSystemApps || it.isUserSensitive }
            .forEach {
                permissionUsageCountMap[it.permissionGroup] =
                    permissionUsageCountMap.getOrDefault(it.permissionGroup, 0) + 1
            }
        return PermissionUsagesUiState.Success(
            permGroupOps.any { !it.isUserSensitive },
            permissionUsageCountMap,
            agentUsages.value!!,
            showSystemApps,
            show7DaysData,
        )
    }

    fun getShowSystemApps(): Boolean = showSystemFlow.value

    fun getShow7DaysData(): Boolean = show7DaysFlow.value

    val showSystemAppsLiveData =
        showSystemFlow.asLiveData(context = coroutineScope.coroutineContext)

    fun updateShowSystem(showSystem: Boolean) {
        if (showSystem != savedState[SHOULD_SHOW_SYSTEM_KEY]) {
            savedState[SHOULD_SHOW_SYSTEM_KEY] = showSystem
        }
        showSystemFlow.compareAndSet(!showSystem, showSystem)
    }

    fun updateShow7Days(show7Days: Boolean) {
        if (show7Days != savedState[SHOULD_SHOW_7_DAYS_KEY]) {
            savedState[SHOULD_SHOW_7_DAYS_KEY] = show7Days
        }
        show7DaysFlow.compareAndSet(!show7Days, show7Days)
    }

    private val permissionGroupLabels = mutableMapOf<String, String>()

    fun getPermissionGroupLabel(permissionGroup: String): String {
        return runBlocking(coroutineScope.coroutineContext + Dispatchers.Default) {
            permissionGroupLabels.getOrDefault(
                permissionGroup,
                permissionRepository.getPermissionGroupLabel(permissionGroup).toString(),
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun getAppFunctionAgentLabel(context: Context, packageName: String): String? {
        return runBlocking(coroutineScope.coroutineContext + Dispatchers.Default) {
            getAppFunctionPackageInfoUseCase(packageName, context, Process.myUserHandle())?.label
        }
    }

    /** Companion class for [PermissionUsageViewModel]. */
    companion object {
        private val TIME_7_DAYS_DURATION = TimeUnit.DAYS.toMillis(7)
        private val TIME_24_HOURS_DURATION = TimeUnit.DAYS.toMillis(1)
    }
}

/** Data class to hold all the information required to configure the UI. */
sealed class PermissionUsagesUiState {
    data object Loading : PermissionUsagesUiState()

    data class Success(
        val containsSystemAppUsage: Boolean,
        val permissionGroupUsageCount: Map<String, Int>,
        val agentUsages: List<AgentActivityItem>,
        val showSystem: Boolean,
        val show7Days: Boolean,
    ) : PermissionUsagesUiState()

    data object Failure : PermissionUsagesUiState()
}

/** Factory for [PermissionUsageViewModel]. */
@RequiresApi(Build.VERSION_CODES.S)
class PermissionUsageViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val permissionRepository = PermissionRepository.getInstance(app)
        val permissionUsageUseCase = GetPermissionGroupUsageUseCase.create(app)
        @SuppressLint("NewApi")
        val appFunctionAgentUsageUseCase =
            if (AppFunctionsUtil.isPrivacyDashboardAgentActivityEnabled(app.applicationContext)) {
                val appInteractionRepository = AppInteractionRepository.getInstance()
                val packageRepository = PackageRepository.getInstance(app)
                GetAgentUsageUseCaseImpl(appInteractionRepository, packageRepository)
            } else {
                NoOpAgentUsageUseCase()
            }
        val appFunctionPackageInfoUseCase =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                val packageRepository = PackageRepository.getInstance(app)
                GetAppFunctionPackageInfoUseCaseImpl(packageRepository)
            } else {
                NoOpAppFunctionPackageInfoUseCase()
            }
        return PermissionUsageViewModel(
            app,
            permissionRepository,
            permissionUsageUseCase,
            appFunctionAgentUsageUseCase,
            appFunctionPackageInfoUseCase,
            savedState = extras.createSavedStateHandle(),
        )
            as T
    }
}
