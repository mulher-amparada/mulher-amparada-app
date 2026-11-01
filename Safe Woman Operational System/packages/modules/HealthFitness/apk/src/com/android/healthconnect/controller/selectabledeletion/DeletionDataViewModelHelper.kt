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
package com.android.healthconnect.controller.selectabledeletion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.healthconnect.controller.permissions.data.HealthPermissionType

/** A base class for the shared functionality of [AllDataViewModel] and [AppDataViewModel] */
abstract class DeletionDataViewModel : ViewModel() {

    private val _setOfPermissionTypesToBeDeleted = MutableLiveData<Set<HealthPermissionType>>()

    val setOfPermissionTypesToBeDeleted: LiveData<Set<HealthPermissionType>>
        get() = _setOfPermissionTypesToBeDeleted

    private val _deletionScreenState = MutableLiveData<DeletionScreenState>()
    val deletionScreenState: LiveData<DeletionScreenState>
        get() = _deletionScreenState

    protected var numOfPermissionTypes: Int = 0
    private val _allPermissionTypesSelected = MutableLiveData<Boolean>()
    val allPermissionTypesSelected: LiveData<Boolean>
        get() = _allPermissionTypesSelected

    fun resetDeletionSet() {
        _setOfPermissionTypesToBeDeleted.value = emptySet()
    }

    fun addToDeletionSet(permissionType: HealthPermissionType) {
        val deleteSet = _setOfPermissionTypesToBeDeleted.value.orEmpty().toMutableSet()
        deleteSet.add(permissionType)
        _setOfPermissionTypesToBeDeleted.value = deleteSet.toSet()
        if (numOfPermissionTypes == deleteSet.size) {
            _allPermissionTypesSelected.postValue(true)
        }
        _deletionScreenState.value = DeletionScreenState.DELETE
    }

    fun removeFromDeletionSet(permissionType: HealthPermissionType) {
        val deleteSet = _setOfPermissionTypesToBeDeleted.value.orEmpty().toMutableSet()
        deleteSet.remove(permissionType)
        _setOfPermissionTypesToBeDeleted.value = deleteSet.toSet()
        if (numOfPermissionTypes != deleteSet.size) {
            _allPermissionTypesSelected.postValue(false)
        }
        _deletionScreenState.value = DeletionScreenState.DELETE
    }

    fun setDeletionScreenStateValue(screenState: DeletionScreenState) {
        if (screenState == DeletionScreenState.VIEW) {
            resetDeletionSet()
        }
        _deletionScreenState.value = screenState
    }

    fun getDeletionScreenStateValue(): DeletionScreenState {
        return _deletionScreenState.value ?: DeletionScreenState.VIEW
    }

    fun getTheNumOfPermissionTypes(): Int = numOfPermissionTypes

    enum class DeletionScreenState {
        VIEW,
        DELETE,
    }
}
