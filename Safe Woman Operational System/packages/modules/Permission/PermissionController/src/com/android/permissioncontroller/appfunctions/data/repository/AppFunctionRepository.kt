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

package com.android.permissioncontroller.appfunctions.data.repository

import android.app.Application
import android.app.appfunctions.AppFunctionManager
import android.app.appfunctions.AppFunctionManager.ACCESS_REQUEST_STATE_UNREQUESTABLE
import android.app.appfunctions.AppFunctionManager.OnAppFunctionAccessChangedListener
import android.permission.flags.Flags
import java.util.concurrent.Executor
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** This repository encapsulate app function data exposed by [AppFunctionManager]. */
interface AppFunctionRepository {
    /**
     * Returns a list of all valid agents. See [AppFunctionManager#getValidAgents] for more details.
     */
    suspend fun getValidAgents(): List<String>

    /**
     * Returns a list of all valid targets. See [AppFunctionManager#getValidTargets] for more
     * details.
     */
    suspend fun getValidTargets(): List<String>

    /**
     * Checks whether the given agent has access to app functions of the given target app. See
     * [AppFunctionManager#getAccessRequestState] for more details.
     *
     * @param agentPackageName The package name of the agent.
     * @param targetPackageName The package name of the target app.
     * @return The state of the access, one of {@link
     *   AppFunctionManager#ACCESS_REQUEST_STATE_GRANTED}, {@link
     *   AppFunctionManager#ACCESS_REQUEST_STATE_DENIED}, or {@link
     *   AppFunctionManager#ACCESS_REQUEST_STATE_UNREQUESTABLE}.
     */
    suspend fun getAccessRequestState(agentPackageName: String, targetPackageName: String): Int

    /**
     * Returns the access flags for the given agent and target package name. See
     * [AppFunctionManager#getAccessFlags] for more details.
     */
    suspend fun getAccessFlags(agentPackageName: String, targetPackageName: String): Int

    /**
     * Updates the access flags for the given agent and target package name. See
     * [AppFunctionManager#updateAccessFlags] for more details.
     */
    suspend fun updateAccessFlags(
        agentPackageName: String,
        targetPackageName: String,
        flagMask: Int,
        flags: Int,
    )

    /**
     * Registers the provided [OnAppFunctionAccessChangedListener]. See
     * [AppFunctionManager#addAccessChangedListener] for more details.
     *
     * @param executor The executor to run the listener callbacks on
     * @param listener The listener to add
     */
    fun addAccessChangedListener(executor: Executor, listener: OnAppFunctionAccessChangedListener)

    /**
     * Unregisters the provided [OnAppFunctionAccessChangedListener]. See
     * [AppFunctionManager#removeAccessChangedListener] for more details.
     *
     * @param listener The listener to remove
     */
    fun removeAccessChangedListener(listener: OnAppFunctionAccessChangedListener)

    companion object {
        const val DEVICE_SETTINGS_TARGET_PACKAGE_NAME = "android"

        @Volatile private var instance: AppFunctionRepository? = null

        /** Returns the singleton instance of [AppFunctionRepository]. */
        fun getInstance(application: Application): AppFunctionRepository =
            instance
                ?: synchronized(this) {
                    AppFunctionRepositoryImpl(application).also { instance = it }
                }
    }
}

/** Implementation of [AppFunctionRepository]. */
class AppFunctionRepositoryImpl(
    application: Application,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AppFunctionRepository {
    private val appFunctionManager: AppFunctionManager? =
        if (Flags.appFunctionAccessApiEnabled()) {
            application.getSystemService(AppFunctionManager::class.java)
        } else {
            null
        }

    override suspend fun getValidAgents(): List<String> =
        withContext(dispatcher) { appFunctionManager?.getValidAgents() ?: emptyList() }

    override suspend fun getValidTargets(): List<String> =
        withContext(dispatcher) { appFunctionManager?.getValidTargets() ?: emptyList() }

    override suspend fun getAccessRequestState(
        agentPackageName: String,
        targetPackageName: String,
    ): Int =
        withContext(dispatcher) {
            appFunctionManager?.getAccessRequestState(agentPackageName, targetPackageName)
                ?: ACCESS_REQUEST_STATE_UNREQUESTABLE
        }

    override suspend fun getAccessFlags(agentPackageName: String, targetPackageName: String): Int =
        withContext(dispatcher) {
            // API returns 0 if the combination is not valid
            appFunctionManager?.getAccessFlags(agentPackageName, targetPackageName) ?: 0
        }

    override suspend fun updateAccessFlags(
        agentPackageName: String,
        targetPackageName: String,
        flagMask: Int,
        flags: Int,
    ) {
        withContext(dispatcher) {
            appFunctionManager?.updateAccessFlags(
                agentPackageName,
                targetPackageName,
                flagMask,
                flags,
            )
        }
    }

    override fun addAccessChangedListener(
        executor: Executor,
        listener: OnAppFunctionAccessChangedListener,
    ) {
        appFunctionManager?.addAccessChangedListener(executor, listener)
    }

    override fun removeAccessChangedListener(listener: OnAppFunctionAccessChangedListener) {
        appFunctionManager?.removeAccessChangedListener(listener)
    }
}
