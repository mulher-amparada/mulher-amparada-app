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

import android.health.connect.HealthConnectException
import android.health.connect.exportimport.ExportImportDocumentProvider
import android.health.connect.exportimport.ScheduledExportSettings
import android.health.connect.exportimport.ScheduledExportStatus
import android.os.OutcomeReceiver
import java.util.concurrent.Executor

/** Wrapper for HealthConnectManager export apis. */
interface HealthDataExportManager {
    fun getScheduledExportPeriodInDays(): Int

    fun configureScheduledExport(settings: ScheduledExportSettings)

    fun getScheduledExportStatus(
        executor: Executor,
        outcomeReceiver: OutcomeReceiver<ScheduledExportStatus, HealthConnectException>
    )

    fun queryDocumentProviders(
        executor: Executor,
        outcomeReceiver: OutcomeReceiver<List<ExportImportDocumentProvider>, HealthConnectException>
    )
}
