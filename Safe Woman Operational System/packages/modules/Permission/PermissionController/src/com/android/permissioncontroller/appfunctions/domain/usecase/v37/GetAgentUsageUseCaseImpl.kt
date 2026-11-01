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
package com.android.permissioncontroller.appfunctions.domain.usecase.v37

import android.content.Context
import android.os.Process
import com.android.permissioncontroller.appfunctions.AppFunctionsUtil
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAgentUsageUseCase
import com.android.permissioncontroller.appinteraction.data.repository.AppInteractionRepository
import com.android.permissioncontroller.appinteraction.domain.model.v31.AgentActivityItem
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * The use case gets the agent usages and returns a map of agent names to their
 * [AgentActivityItem]'s. The AccessCount contains the number of agent access to their target apps
 * in the past 24 hours and 7 days.
 *
 * @param appFunctionRepository The repository to use to get the app function agents.
 */
class GetAgentUsageUseCaseImpl(
    private val appInteractionRepository: AppInteractionRepository,
    private val packageRepository: PackageRepository,
) : GetAgentUsageUseCase {
    override suspend operator fun invoke(context: Context): List<AgentActivityItem> {
        if (!AppFunctionsUtil.isPrivacyDashboardAgentActivityEnabled(context)) {
            return emptyList()
        }

        /*
         * Currently we don't support execution of app function for profile users. Hence, we only
         * fetch access histories for the current user. Once we start supporting profile users, we
         * should do the below to fetch profile user usages
         *
         * val profiles =
         *     userRepository
         *         .getUserProfilesIncludingCurrentUser()
         *         .filterUsersToShowInQuietMode(userRepository)
         *         .filter { userRepository.isUserUnlocked(it) }
         *         .map { UserHandle.of(it) }
         */
        val profiles = listOf(Process.myUserHandle())
        val deviceAssistancePackageNames =
            appInteractionRepository.getDeviceAssistancePackageNames(context).toSet()
        val agentUsages = mutableListOf<AgentActivityItem>()

        profiles.forEach { user ->
            val agents =
                packageRepository.getPackagesHoldingPermissions(AGENT_PERMISSIONS, user).filter {
                    it !in HIDDEN_WHEN_NO_ACTIVITIES_PACKAGES
                }
            val accessHistories = appInteractionRepository.getAccessHistory(context, user)
            val userAgentUsages =
                agents.associateWith { AgentActivityItem(it, user, 0, 0) }.toMutableMap()

            val now = System.currentTimeMillis()
            val timeStamp24Hours =
                max(now - TimeUnit.DAYS.toMillis(1), Instant.EPOCH.toEpochMilli())
            val timeStamp7Days = max(now - TimeUnit.DAYS.toMillis(7), Instant.EPOCH.toEpochMilli())

            accessHistories
                .groupBy { it.agentPackageName }
                .forEach { (agentPackageName, accessHistories) ->
                    val distinctAccessCount24Hours = mutableSetOf<String>()
                    val distinctAccessCount7Days = mutableSetOf<String>()
                    for (accessHistory in accessHistories) {
                        val targetPackageName =
                            if (accessHistory.targetPackageName in deviceAssistancePackageNames) {
                                DEVICE_ASSISTANCE_TARGET_PACKAGE_NAME
                            } else {
                                accessHistory.targetPackageName
                            }

                        if (accessHistory.accessTime > timeStamp24Hours) {
                            distinctAccessCount24Hours.add(targetPackageName)
                        }
                        if (accessHistory.accessTime > timeStamp7Days) {
                            distinctAccessCount7Days.add(targetPackageName)
                        }
                    }

                    userAgentUsages[agentPackageName] =
                        AgentActivityItem(
                            agentPackageName,
                            user,
                            distinctAccessCount24Hours.size,
                            distinctAccessCount7Days.size,
                        )
                }

            agentUsages.addAll(userAgentUsages.values)
        }

        return agentUsages
    }

    companion object {
        private const val DEVICE_ASSISTANCE_TARGET_PACKAGE_NAME = "DEVICE_ASSISTANCE"

        private val AGENT_PERMISSIONS =
            listOf(
                android.Manifest.permission.EXECUTE_APP_FUNCTIONS,
                // While this permission should be a @SystemApi, it is actually defined as a @hide
                // permission. Hence, we can only access this permission via raw string.
                "android.permission.ACCESS_COMPUTER_CONTROL",
            )

        private val HIDDEN_WHEN_NO_ACTIVITIES_PACKAGES = listOf("com.android.shell")
    }
}
