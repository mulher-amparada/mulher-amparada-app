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

package com.android.permissioncontroller.appinteraction.domain.model.v31

import android.os.UserHandle

/**
 * Model for an app function agent or target package.
 *
 * @param accessCount24Hours The count of target apps that an agent has accessed in past 24 hours
 * @param accessCount7Days The count of target apps that an agent has accessed in past 7 days
 */
data class AgentActivityItem(
    val agentPackageName: String,
    val userHandle: UserHandle,
    val accessCount24Hours: Int,
    val accessCount7Days: Int,
)
