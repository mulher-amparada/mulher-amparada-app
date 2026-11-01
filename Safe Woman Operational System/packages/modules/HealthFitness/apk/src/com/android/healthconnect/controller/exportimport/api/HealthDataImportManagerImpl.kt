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
import android.health.connect.HealthConnectManager
import android.health.connect.exportimport.ImportStatus
import android.net.Uri
import android.os.OutcomeReceiver
import java.util.concurrent.Executor
import javax.inject.Inject

/** Implementation of the HealthImportManager interface. */
class HealthDataImportManagerImpl @Inject constructor(private val manager: HealthConnectManager) :
    HealthDataImportManager {

    override fun getImportStatus(
        executor: Executor,
        outcomeReceiver: OutcomeReceiver<ImportStatus, HealthConnectException>
    ) {
        return manager.getImportStatus(executor, outcomeReceiver)
    }

    override fun runImport(
        uri: Uri,
        executor: Executor,
        outcomeReceiver: OutcomeReceiver<Void, HealthConnectException>
    ) {
        return manager.runImport(uri, executor, outcomeReceiver)
    }
}
