/**
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * ```
 *    http://www.apache.org/licenses/LICENSE-2.0
 * ```
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.testapps.toolbox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.health.connect.HealthConnectManager
import android.health.connect.ReadRecordsRequestUsingFilters
import android.health.connect.TimeInstantRangeFilter
import android.health.connect.datatypes.DataOrigin
import android.health.connect.datatypes.Record
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.readRecords
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.requireByteArrayExtra
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.requireSerializable
import com.android.healthconnect.testapps.toolbox.utils.GeneralUtils.Companion.requireSystemService
import com.android.healthconnect.testapps.toolbox.utils.asString
import com.android.healthconnect.testapps.toolbox.utils.deserialize
import com.android.healthconnect.testapps.toolbox.utils.launchFuture
import com.android.healthconnect.testapps.toolbox.utils.serialize
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.io.Serializable
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * Calls the [ToolboxProxyReceiver] of the Toolbox app specified by the [packageName] argument
 * and returns a response.
 **/
suspend fun callToolbox(
    context: Context,
    packageName: String,
    payload: ToolboxProxyPayload,
): String? {
    val requestId = UUID.randomUUID().toString()

    context.sendToolboxRequest(
        toolboxPackageName = packageName,
        request = ToolboxProxyRequest(
            id = requestId,
            callerPackageName = context.packageName,
            payload = payload,
        ),
    )

    return receiveToolboxResponse(requestId = requestId)
}

/** A payload to be sent to [ToolboxProxyReceiver] as part of a request. */
sealed interface ToolboxProxyPayload : Serializable {

    /**
     * Reads records of the specified [type] contributed by apps specified by the [packageNames]
     * list, or by all apps if the [packageNames] list is empty.
     */
    data class ReadRecords(
        val type: Class<out Record>,
        val packageNames: List<String> = emptyList(),
    ) : ToolboxProxyPayload
}

private data class ToolboxProxyRequest(
    val id: String,
    val callerPackageName: String,
    val payload: ToolboxProxyPayload,
) : Serializable

private data class ToolboxProxyResponse(
    val requestId: String,
    val response: String,
) : Serializable

private fun Context.sendToolboxRequest(toolboxPackageName: String, request: ToolboxProxyRequest) {
    sendBroadcast(
        Intent(ACTION_REQUEST)
            .setClassName(toolboxPackageName, ToolboxProxyReceiver::class.java.name)
            .putExtra(EXTRA_REQUEST, request.serialize())
    )
}

private fun Context.sendToolboxResponse(
    toolboxPackageName: String,
    response: ToolboxProxyResponse,
) {
    sendBroadcast(
        Intent(ACTION_RESPONSE)
            .setClassName(toolboxPackageName, ToolboxProxyReceiver::class.java.name)
            .putExtra(EXTRA_RESPONSE, response)
    )
}

const val PKG_TOOLBOX_2: String = "com.android.healthconnect.testapps.toolbox2"

private const val ACTION_REQUEST = "action.REQUEST"
private const val ACTION_RESPONSE = "action.RESPONSE"
private const val EXTRA_RESPONSE = "extra.RESPONSE"
private const val EXTRA_REQUEST = "extra.REQUEST"

private val responseFlow =
    MutableSharedFlow<ToolboxProxyResponse>(extraBufferCapacity = Int.MAX_VALUE)

private suspend fun receiveToolboxResponse(requestId: String): String? =
    withTimeoutOrNull(timeout = 5.seconds) {
        responseFlow.first { it.requestId == requestId }.response
    }

class ToolboxProxyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_REQUEST -> {
                enqueueWorker(
                    context = context,
                    requestBytes = intent.requireByteArrayExtra(EXTRA_REQUEST),
                )
            }

            ACTION_RESPONSE -> {
                responseFlow.tryEmit(
                    intent.requireSerializable<ToolboxProxyResponse>(EXTRA_RESPONSE)
                )
            }
        }
    }

    private fun enqueueWorker(context: Context, requestBytes: ByteArray) {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<ToolboxProxyWorker>()
                .setInputData(workDataOf(EXTRA_REQUEST to requestBytes))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        )
    }
}

class ToolboxProxyWorker(
    private val context: Context,
    params: WorkerParameters,
) : ListenableWorker(context, params) {

    private val manager = context.requireSystemService<HealthConnectManager>()

    private val request =
        requireNotNull(inputData.getByteArray(EXTRA_REQUEST)).deserialize<ToolboxProxyRequest>()

    override fun startWork(): ListenableFuture<Result> =
        launchFuture {
            try {
                sendResponse(doWork())
                Result.success()
            } catch (e: Exception) {
                sendResponse(e.toString())
                Result.failure()
            }
        }

    private suspend fun doWork(): String =
        when (val payload = request.payload) {
            is ToolboxProxyPayload.ReadRecords -> readRecords(payload)
        }

    private fun sendResponse(response: String) {
        context.sendToolboxResponse(
            toolboxPackageName = request.callerPackageName,
            response = ToolboxProxyResponse(requestId = request.id, response = response),
        )
    }

    private suspend inline fun readRecords(payload: ToolboxProxyPayload.ReadRecords): String =
        readRecords(
            manager = manager,
            request = ReadRecordsRequestUsingFilters.Builder(payload.type)
                .setTimeRangeFilter(
                    TimeInstantRangeFilter.Builder()
                        .setStartTime(Instant.EPOCH)
                        .setEndTime(Instant.now())
                        .build()
                )
                .apply {
                    for (pkg in payload.packageNames) {
                        addDataOrigins(DataOrigin.Builder().setPackageName(pkg).build())
                    }
                }
                .build(),
        ).joinToString(separator = "\n", transform = Record::asString)
}
