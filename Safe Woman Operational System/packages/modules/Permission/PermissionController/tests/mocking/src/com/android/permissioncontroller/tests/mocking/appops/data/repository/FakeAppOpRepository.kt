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

package com.android.permissioncontroller.tests.mocking.appops.data.repository

import android.app.AppOpsManager
import android.app.AppOpsManager.OnOpChangedListener
import com.android.permissioncontroller.appops.data.model.v31.DiscretePackageOpsModel
import com.android.permissioncontroller.appops.data.model.v31.PackageAppOpUsageModel
import com.android.permissioncontroller.appops.data.repository.v31.AppOpRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAppOpRepository(
    override val packageAppOpsUsages: Flow<List<PackageAppOpUsageModel>>,
    private val discreteOps: Flow<List<DiscretePackageOpsModel>> = flowOf(),
) : AppOpRepository {
    private val opModes: MutableMap<Pair<String, Int>, Int> = mutableMapOf()
    private val onOpChangedListeners: MutableList<OnOpChangedListener> = mutableListOf()

    override fun getDiscreteOps(
        opNames: List<String>,
        coroutineScope: CoroutineScope,
    ): Flow<List<DiscretePackageOpsModel>> {
        return discreteOps
    }

    override fun checkOpNoThrow(op: String, uid: Int, packageName: String): Int =
        opModes.getOrDefault(op to uid, AppOpsManager.MODE_ERRORED)

    override fun setUidMode(op: String, uid: Int, mode: Int) {
        opModes.put(op to uid, mode)
        onOpChangedListeners.forEach { it.onOpChanged(op, "") }
    }

    // TODO: support watcher that filters based on package name
    override fun startWatchingMode(op: String, packageName: String, callback: OnOpChangedListener) {
        onOpChangedListeners.add(callback)
    }

    override fun stopWatchingMode(callback: OnOpChangedListener) {
        onOpChangedListeners.remove(callback)
    }
}
