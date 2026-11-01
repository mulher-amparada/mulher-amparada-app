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

package com.android.healthconnect.controller.tests.utils.di

import android.health.connect.HealthConnectException
import android.health.connect.exportimport.ExportImportDocumentProvider
import android.health.connect.exportimport.ScheduledExportSettings
import android.health.connect.exportimport.ScheduledExportStatus
import android.net.Uri
import android.os.OutcomeReceiver
import com.android.healthconnect.controller.exportimport.api.ExportFrequency
import com.android.healthconnect.controller.exportimport.api.HealthDataExportManager
import java.util.concurrent.Executor

class FakeHealthDataExportManager : HealthDataExportManager {

    companion object {
        private val DEFAULT_SCHEDULED_EXPORT_STATUS =
            ScheduledExportStatus.Builder()
                .setDataExportError(ScheduledExportStatus.DATA_EXPORT_ERROR_NONE)
                .setPeriodInDays(ExportFrequency.EXPORT_FREQUENCY_NEVER.periodInDays)
                .build()
    }

    private var exportUri: Uri? = null
    private var scheduledExportStatus: ScheduledExportStatus = DEFAULT_SCHEDULED_EXPORT_STATUS
    private var documentProviders = emptyList<ExportImportDocumentProvider>()

    private var getScheduledExportPeriodInDaysException: HealthConnectException? = null
    private var scheduledExportStatusException: HealthConnectException? = null
    private var queryDocumentProvidersException: HealthConnectException? = null
    private var configureScheduledExportException: HealthConnectException? = null

    override fun getScheduledExportPeriodInDays(): Int {
        getScheduledExportPeriodInDaysException?.let { throw it }
            ?: run {
                return scheduledExportStatus.periodInDays
            }
    }

    override fun configureScheduledExport(settings: ScheduledExportSettings) {
        configureScheduledExportException?.let { throw it }
            ?: run {
                if (settings.periodInDays >= 0) {
                    scheduledExportStatus =
                        ScheduledExportStatus.Builder()
                            .setDataExportError(scheduledExportStatus.dataExportError)
                            .setLastExportAppName(scheduledExportStatus.lastExportAppName)
                            .setLastExportFileName(scheduledExportStatus.lastExportFileName)
                            .setLastSuccessfulExportTime(
                                scheduledExportStatus.lastSuccessfulExportTime
                            )
                            .setLastFailedExportTime(scheduledExportStatus.lastFailedExportTime)
                            .setNextExportAppName(scheduledExportStatus.nextExportAppName)
                            .setNextExportFileName(scheduledExportStatus.nextExportFileName)
                            .setPeriodInDays(settings.periodInDays)
                            .build()
                }
                if (settings.uri != null) {
                    exportUri = settings.uri
                }
            }
    }

    override fun getScheduledExportStatus(
        executor: Executor,
        outcomeReceiver: OutcomeReceiver<ScheduledExportStatus, HealthConnectException>,
    ) {
        scheduledExportStatusException?.let { outcomeReceiver.onError(it) }
            ?: run { outcomeReceiver.onResult(scheduledExportStatus) }
    }

    override fun queryDocumentProviders(
        executor: Executor,
        outcomeReceiver: OutcomeReceiver<List<ExportImportDocumentProvider>, HealthConnectException>,
    ) {
        queryDocumentProvidersException?.let { outcomeReceiver.onError(it) }
            ?: run { outcomeReceiver.onResult(documentProviders) }
    }

    fun getExportUri(): Uri? {
        return exportUri
    }

    fun setGetScheduledPeriodInDaysException(exception: HealthConnectException?) {
        getScheduledExportPeriodInDaysException = exception
    }

    fun setScheduledExportStatus(scheduledExportStatus: ScheduledExportStatus) {
        this.scheduledExportStatus = scheduledExportStatus
    }

    fun setExportImportDocumentProviders(documentProviders: List<ExportImportDocumentProvider>) {
        this.documentProviders = documentProviders
    }

    fun setScheduledExportStatusException(exception: HealthConnectException?) {
        scheduledExportStatusException = exception
    }

    fun setQueryDocumentProvidersException(exception: HealthConnectException?) {
        queryDocumentProvidersException = exception
    }

    fun setConfigureScheduledExportException(exception: HealthConnectException?) {
        configureScheduledExportException = exception
    }

    fun reset() {
        exportUri = null
        scheduledExportStatus = DEFAULT_SCHEDULED_EXPORT_STATUS
        documentProviders = emptyList()
        scheduledExportStatusException = null
        queryDocumentProvidersException = null
        getScheduledExportPeriodInDaysException = null
        configureScheduledExportException = null
    }
}
