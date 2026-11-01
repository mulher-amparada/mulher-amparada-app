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
package com.android.permissioncontroller.tests.mocking.appfunctions.data.repository

import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_DENIED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_GRANTED
import android.app.appfunctions.AppFunctionManager.ACCESS_REQUEST_STATE_DENIED
import android.app.appfunctions.AppFunctionManager.ACCESS_REQUEST_STATE_GRANTED
import android.app.appfunctions.AppFunctionManager.ACCESS_REQUEST_STATE_UNREQUESTABLE
import android.app.appfunctions.AppFunctionManager.OnAppFunctionAccessChangedListener
import com.android.permissioncontroller.appfunctions.data.repository.AppFunctionRepository
import java.util.concurrent.Executor

/** Fake implementation of [AppFunctionRepository] for testing. */
class FakeAppFunctionRepository(
    private val agents: List<String> = emptyList(),
    private val targets: List<String> = emptyList(),
    accessFlags: Map<Pair<String, String>, Int> = mutableMapOf(),
) : AppFunctionRepository {
    private val _accessFlags: MutableMap<Pair<String, String>, Int> = accessFlags.toMutableMap()
    private val listeners: MutableSet<OnAppFunctionAccessChangedListener> = mutableSetOf()

    override suspend fun getValidAgents(): List<String> = agents

    override suspend fun getValidTargets(): List<String> = targets

    override suspend fun getAccessRequestState(
        agentPackageName: String,
        targetPackageName: String,
    ): Int {
        val flags = _accessFlags.getOrDefault(agentPackageName to targetPackageName, 0)
        return when {
            (flags and FLAG_MASK) == ACCESS_FLAG_USER_GRANTED -> ACCESS_REQUEST_STATE_GRANTED
            (flags and FLAG_MASK) == ACCESS_FLAG_USER_DENIED -> ACCESS_REQUEST_STATE_DENIED
            else -> ACCESS_REQUEST_STATE_UNREQUESTABLE
        }
    }

    override suspend fun getAccessFlags(agentPackageName: String, targetPackageName: String): Int =
        _accessFlags.getOrDefault(agentPackageName to targetPackageName, 0)

    override suspend fun updateAccessFlags(
        agentPackageName: String,
        targetPackageName: String,
        flagMask: Int,
        flags: Int,
    ) {
        _accessFlags[agentPackageName to targetPackageName] = flagMask and flags
        notifyListeners()
    }

    override fun addAccessChangedListener(
        executor: Executor,
        listener: OnAppFunctionAccessChangedListener,
    ) {
        listeners.add(listener)
    }

    override fun removeAccessChangedListener(listener: OnAppFunctionAccessChangedListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        // TODO: Implement agentPackageName to agentUid
        listeners.forEach { listener -> listener.onAppFunctionAccessChanged(0) }
    }

    companion object {
        private const val FLAG_MASK = ACCESS_FLAG_USER_GRANTED or ACCESS_FLAG_USER_DENIED
    }
}
