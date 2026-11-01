/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.permissioncontroller.appinteraction.domain.model.v37

import android.os.UserHandle

/**
 * Data class for the agent access entries on the agent timeline dashboard
 *
 * @param agentUid The UID of the agent app
 * @param agentPackageName The package name of the agent app
 * @param targetPackageName The package name of the target app
 * @param user The UserHandle for the agent timeline
 * @param lastAccessTime The timestamp (in milliseconds) when the app interaction was accessed
 * @param interactionUri A URI linking to the original interaction context
 * @param isDeviceAssistanceAccess Whether the target app is a device assistance
 */
data class AgentTimelineItem(
    val agentUid: Int,
    val agentPackageName: String,
    val targetPackageName: String,
    val user: UserHandle,
    val lastAccessTime: Long,
    val interactionUri: String?,
    val isDeviceAssistanceAccess: Boolean,
)
