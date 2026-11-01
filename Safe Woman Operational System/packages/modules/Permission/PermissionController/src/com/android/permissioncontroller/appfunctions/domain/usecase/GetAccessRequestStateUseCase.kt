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
package com.android.permissioncontroller.appfunctions.domain.usecase

import android.app.appfunctions.AppFunctionManager.ACCESS_REQUEST_STATE_GRANTED
import android.app.appfunctions.AppFunctionManager.ACCESS_REQUEST_STATE_UNREQUESTABLE
import com.android.permissioncontroller.appfunctions.data.repository.AppFunctionRepository

/**
 * This use case gets a map of access request states for a specified agent and target packages.
 *
 * <p> Filters out [ACCESS_REQUEST_STATE_UNREQUESTABLE] and maps [ACCESS_REQUEST_STATE_GRANTED] and
 * [ACCESS_REQUEST_STATE_DENIED] to true and false respectively
 *
 * @param appFunctionRepository The repository to use to get the app function agents.
 */
class GetAccessRequestStateUseCase(private val appFunctionRepository: AppFunctionRepository) {
    suspend operator fun invoke(
        agentPackageName: String,
        targetPackageNames: List<String>,
    ): Map<String, Boolean> {
        val agentTargetAccessMap = mutableMapOf<String, Boolean>()
        for (targetPackageName in targetPackageNames) {
            val accessRequestState =
                appFunctionRepository.getAccessRequestState(agentPackageName, targetPackageName)
            if (accessRequestState == ACCESS_REQUEST_STATE_UNREQUESTABLE) {
                continue
            }
            agentTargetAccessMap[targetPackageName] =
                accessRequestState == ACCESS_REQUEST_STATE_GRANTED
        }
        return agentTargetAccessMap
    }

    suspend operator fun invoke(
        agentPackageNames: List<String>,
        targetPackageName: String,
    ): Map<String, Boolean> {
        val agentTargetAccessMap = mutableMapOf<String, Boolean>()
        for (agentPackageName in agentPackageNames) {
            val accessRequestState =
                appFunctionRepository.getAccessRequestState(agentPackageName, targetPackageName)
            if (accessRequestState == ACCESS_REQUEST_STATE_UNREQUESTABLE) {
                continue
            }
            agentTargetAccessMap[agentPackageName] =
                accessRequestState == ACCESS_REQUEST_STATE_GRANTED
        }
        return agentTargetAccessMap
    }

    suspend operator fun invoke(agentPackageName: String, targetPackageName: String): Int =
        appFunctionRepository.getAccessRequestState(agentPackageName, targetPackageName)
}
