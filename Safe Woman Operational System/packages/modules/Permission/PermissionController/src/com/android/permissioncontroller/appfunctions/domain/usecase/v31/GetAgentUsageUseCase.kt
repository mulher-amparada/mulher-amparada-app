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
package com.android.permissioncontroller.appfunctions.domain.usecase.v31

import android.content.Context
import com.android.permissioncontroller.appinteraction.domain.model.v31.AgentActivityItem

/** A use case for getting the usage of agents. */
interface GetAgentUsageUseCase {
    /**
     * Retrieves the usages for agents.
     *
     * @param context The [Context] of the application.
     */
    suspend operator fun invoke(context: Context): List<AgentActivityItem>
}
