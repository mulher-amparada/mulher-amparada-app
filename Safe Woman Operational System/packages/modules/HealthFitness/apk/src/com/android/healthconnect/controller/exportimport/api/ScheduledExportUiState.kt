/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.healthconnect.controller.exportimport.api

import java.time.Instant

/**
 * Internal class representing the [ScheduledExportStatus] received from the HealthConnectManager.
 */
data class ScheduledExportUiState(
    val lastSuccessfulExportTime: Instant? = null,
    val dataExportError: DataExportError,
    val periodInDays: Int,
    val lastExportFileName: String? = null,
    val lastExportAppName: String? = null,
    val nextExportFileName: String? = null,
    val nextExportAppName: String? = null,
    val lastFailedExportTime: Instant? = null,
    val nextExportSequentialNumber: Int? = null,
) {
    enum class DataExportError {
        DATA_EXPORT_ERROR_UNKNOWN,
        DATA_EXPORT_ERROR_NONE,
        DATA_EXPORT_LOST_FILE_ACCESS,
    }
}
