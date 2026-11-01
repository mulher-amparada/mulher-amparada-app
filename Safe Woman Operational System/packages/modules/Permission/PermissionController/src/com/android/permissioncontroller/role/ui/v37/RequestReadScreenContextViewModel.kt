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

package com.android.permissioncontroller.role.ui.v37

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.Application
import android.app.voiceinteraction.VoiceInteractionManager
import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.android.permissioncontroller.appops.data.repository.v31.AppOpRepository
import com.android.permissioncontroller.permission.data.repository.v31.PermissionRepository
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Viewmodel to support read screen context request screen */
class RequestReadScreenContextViewModel(
    application: Application,
    private val packageUid: Int,
    private val appOpRepository: AppOpRepository,
    private val voiceInteractionManager: VoiceInteractionManager,
    scope: CoroutineScope? = null,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AndroidViewModel(application) {
    private val coroutineScope = scope ?: viewModelScope

    fun setAllowed() {
        coroutineScope.launch(dispatcher) {
            appOpRepository.setUidMode(
                AppOpsManager.OPSTR_READ_SCREEN_CONTEXT,
                packageUid,
                MODE_ALLOWED,
            )
            voiceInteractionManager.clearReadScreenContextRequestDeniedCount()
        }
    }

    fun markRequestDenied() {
        coroutineScope.launch(dispatcher) {
            voiceInteractionManager.incrementReadScreenContextRequestDeniedCount()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
class RequestReadScreenContextViewModelFactory(
    private val application: Application,
    private val packageName: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val permissionRepository = PermissionRepository.getInstance(application)
        val appOpsRepository = AppOpRepository.getInstance(application, permissionRepository)
        val packageRepository = PackageRepository.getInstance(application)
        val voiceInteractionManager =
            application.getSystemService(VoiceInteractionManager::class.java)
        return RequestReadScreenContextViewModel(
            application,
            packageRepository.getPackageUid(packageName, Process.myUserHandle()),
            appOpsRepository,
            voiceInteractionManager,
        )
            as T
    }
}
