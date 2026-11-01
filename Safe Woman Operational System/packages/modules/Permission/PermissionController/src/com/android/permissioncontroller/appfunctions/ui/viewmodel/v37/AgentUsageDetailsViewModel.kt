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

package com.android.permissioncontroller.appfunctions.ui.viewmodel.v37

import android.app.Application
import android.os.UserHandle
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.permissioncontroller.appfunctions.domain.usecase.v37.GetAgentUsageDetailsUseCase
import com.android.permissioncontroller.appfunctions.domain.usecase.v37.GetAgentUsageDetailsUseCase.Companion.KEY_PAST_24_HOURS
import com.android.permissioncontroller.appfunctions.domain.usecase.v37.GetAgentUsageDetailsUseCase.Companion.KEY_PAST_7_DAYS
import com.android.permissioncontroller.appinteraction.data.repository.AppInteractionRepository
import com.android.permissioncontroller.appinteraction.domain.model.v37.AgentTimelineItem
import com.android.permissioncontroller.common.model.Stateful
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository
import kotlin.String
import kotlin.collections.Map
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/** The ViewModel for the agents timeline page. */
class AgentUsageDetailsViewModel(
    app: Application,
    private val agentPackageName: String,
    private val user: UserHandle,
    private val packageRepository: PackageRepository,
    private val getAppFunctionAgentUsageDetailsUseCase: GetAgentUsageDetailsUseCase,
    val state: SavedStateHandle = SavedStateHandle(emptyMap()),
    scope: CoroutineScope? = null,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AndroidViewModel(app) {
    private val coroutineScope = scope ?: viewModelScope

    private val show7DaysFlow = MutableStateFlow(state[KEY_SHOULD_SHOW_7_DAYS] ?: false)

    fun getShow7Days(): Boolean = show7DaysFlow.value

    private val agentUsageDetailsStateFlow =
        MutableStateFlow<Stateful<Map<String, List<AgentTimelineItem>>>>(Stateful.Loading())

    init {
        coroutineScope.launch(defaultDispatcher) {
            try {
                val agentUsageDetailsUseCase =
                    getAppFunctionAgentUsageDetailsUseCase(
                        app.applicationContext,
                        agentPackageName,
                        user,
                    )
                agentUsageDetailsStateFlow.value = Stateful.Success(agentUsageDetailsUseCase)
            } catch (e: Exception) {
                agentUsageDetailsStateFlow.value = Stateful.Failure(throwable = e)
            }
        }
    }

    @VisibleForTesting
    val agentUsageDetailsUiDataFlow: Flow<AgentUsageDetailsUiState> by lazy {
        combine(agentUsageDetailsStateFlow, show7DaysFlow) { agentUsageDetailsState, show7Days ->
                buildAgentUsageDetailsUiState(agentUsageDetailsState, show7Days)
            }
            .flowOn(defaultDispatcher)
    }

    private fun buildAgentUsageDetailsUiState(
        agentUsageDetailsState: Stateful<Map<String, List<AgentTimelineItem>>>,
        show7Days: Boolean,
    ): AgentUsageDetailsUiState {
        when (agentUsageDetailsState) {
            is Stateful.Loading -> return AgentUsageDetailsUiState.Loading
            is Stateful.Failure ->
                return AgentUsageDetailsUiState.Failure(agentUsageDetailsState.throwable)
            is Stateful.Success -> {
                val show7DaysKey =
                    if (show7Days) {
                        KEY_PAST_7_DAYS
                    } else {
                        KEY_PAST_24_HOURS
                    }
                val agentUsageDetails = agentUsageDetailsState.value[show7DaysKey]
                val agentTimelineItems = agentUsageDetails ?: emptyList()
                return AgentUsageDetailsUiState.Success(
                    packageRepository.getPackageUid(agentPackageName, user),
                    agentPackageName,
                    getSettingsPackageName(user)!!,
                    agentTimelineItems,
                    show7Days,
                )
            }
        }
    }

    fun updateShow7DaysToggle(show7Days: Boolean) {
        if (show7Days != state[KEY_SHOULD_SHOW_7_DAYS]) {
            state[KEY_SHOULD_SHOW_7_DAYS] = show7Days
        }
        show7DaysFlow.compareAndSet(!show7Days, show7Days)
    }

    /**
     * Returns the package name for the Settings App. See more in
     * [PackageRepository.getSettingsPackageName]
     */
    fun getSettingsPackageName(user: UserHandle): String? =
        packageRepository.getSettingsPackageName(user)

    companion object {
        private const val KEY_SHOULD_SHOW_7_DAYS = "show7Days"
    }
}

sealed class AgentUsageDetailsUiState {
    data object Loading : AgentUsageDetailsUiState()

    data class Failure(val throwable: Throwable) : AgentUsageDetailsUiState()

    data class Success(
        val agentUid: Int,
        val agentPackageName: String,
        val settingsPackageName: String,
        val agentTimelineItems: List<AgentTimelineItem>,
        val show7Days: Boolean,
    ) : AgentUsageDetailsUiState()
}

/** Factory for [AgentUsageDetailsViewModel]. */
class AgentUsageDetailsViewModelFactory(
    val app: Application,
    private val agentPackageName: String,
    private val user: UserHandle,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val packageRepository = PackageRepository.getInstance(app)
        val appInteractionRepository = AppInteractionRepository.getInstance()
        val useCase = GetAgentUsageDetailsUseCase(appInteractionRepository, packageRepository)
        return AgentUsageDetailsViewModel(
            app,
            agentPackageName,
            user,
            packageRepository,
            useCase,
            extras.createSavedStateHandle(),
        )
            as T
    }
}
