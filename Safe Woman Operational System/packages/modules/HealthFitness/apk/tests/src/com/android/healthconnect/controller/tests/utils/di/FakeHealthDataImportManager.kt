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
import android.health.connect.exportimport.ImportStatus
import android.net.Uri
import android.os.OutcomeReceiver
import com.android.healthconnect.controller.exportimport.api.HealthDataImportManager
import java.util.concurrent.Executor

class FakeHealthDataImportManager : HealthDataImportManager {
    companion object {
        private val DEFAULT_IMPORT_STATUS =
            ImportStatus(
                ImportStatus.DATA_IMPORT_ERROR_NONE)
    }

    private var importStatus: ImportStatus = DEFAULT_IMPORT_STATUS
    private var getImportStatusException: HealthConnectException? = null
    private var runImportException: HealthConnectException? = null

    private var importFileUri: Uri? = null

    override fun runImport(
        uri: Uri,
        executor: Executor,
        outcomeReceiver: OutcomeReceiver<Void, HealthConnectException>
    ) {
        importStatus
        runImportException?.let { outcomeReceiver.onError(it) }
            ?: run { outcomeReceiver.onResult(null) }
    }

    fun setRunImportException(exception: HealthConnectException?) {
        runImportException = exception
    }

    override fun getImportStatus(
        executor: Executor,
        outcomeReceiver: OutcomeReceiver<ImportStatus, HealthConnectException>
    ) {
        getImportStatusException?.let { outcomeReceiver.onError(it) }
            ?: run { outcomeReceiver.onResult(importStatus) }
    }

    fun setImportStatus(importStatus: ImportStatus) {
        this.importStatus = importStatus
    }

    fun setGetImportStatusException(exception: HealthConnectException?) {
        getImportStatusException = exception
    }

    fun reset() {
        importStatus = DEFAULT_IMPORT_STATUS
        getImportStatusException = null
        runImportException = null
        importFileUri = null
    }
}
