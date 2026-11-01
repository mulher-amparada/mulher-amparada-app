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
package com.android.permissioncontroller.permission.ui.model.v37

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import android.app.admin.DevicePolicyManager
import android.app.permissionui.LocationButtonClient
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Process
import android.os.RemoteCallback
import android.os.UserHandle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.permissioncontroller.PermissionControllerStatsLog
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__PERMISSION__ACCESS_FINE_LOCATION
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__ALREADY_GRANTED
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__CANCELED
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__DENIED
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__DIRECT_GRANT
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__NOT_GRANTABLE
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__POLICY_AUTO_DENIED
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__POLICY_AUTO_GRANTED
import com.android.permissioncontroller.PermissionControllerStatsLog.LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__PROMPT_GRANT
import com.android.permissioncontroller.permission.data.LightAppPermGroupLiveData
import com.android.permissioncontroller.permission.data.get
import com.android.permissioncontroller.permission.model.livedatatypes.LightAppPermGroup
import com.android.permissioncontroller.permission.utils.KotlinUtils
import com.android.permissioncontroller.permission.utils.Utils

/** View model for granting location permissions when user interacts with a location button. */
class LocationButtonViewModel(
    val app: Application,
    private val sessionId: Long,
    private val packageName: String,
    private val remoteCallback: RemoteCallback,
) : AndroidViewModel(app) {
    val user: UserHandle = Process.myUserHandle()
    val lightAppPermGroupLiveData =
        LightAppPermGroupLiveData[packageName, Manifest.permission_group.LOCATION, user]

    val locationButtonRequestStateLiveData: LiveData<LocationButtonRequestState> =
        MediatorLiveData<LocationButtonRequestState>().apply {
            addSource(lightAppPermGroupLiveData) { group ->
                if (group == null) {
                    value = LocationButtonRequestState.LOADING
                    return@addSource
                }

                val isGranted = group.permissions[ACCESS_FINE_LOCATION]?.isGranted == true
                val newState =
                    when {
                        isGranted -> {
                            logLocationButtonRequestResult(
                                LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__ALREADY_GRANTED
                            )
                            LocationButtonRequestState.ALREADY_GRANTED
                        }
                        isPermissionNotGrantable(group) -> {
                            logLocationButtonRequestResult(
                                LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__NOT_GRANTABLE
                            )
                            LocationButtonRequestState.NOT_GRANTABLE
                        }
                        else -> {
                            val policy =
                                KotlinUtils.applyPermissionPolicy(app, ACCESS_FINE_LOCATION, group)
                            when (policy) {
                                DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT -> {
                                    logLocationButtonRequestResult(
                                        LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__POLICY_AUTO_GRANTED
                                    )
                                    LocationButtonRequestState.AUTO_GRANTED
                                }
                                DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY -> {
                                    logLocationButtonRequestResult(
                                        LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__POLICY_AUTO_DENIED
                                    )
                                    LocationButtonRequestState.AUTO_DENIED
                                }
                                else -> {
                                    if (group.isTrustedUiConsented) {
                                        LocationButtonRequestState.CONSENTED
                                    } else {
                                        LocationButtonRequestState.SHOW_UI
                                    }
                                }
                            }
                        }
                    }

                Log.d(LOG_TAG, "requestStateLiveData is initialized as $newState.")
                value = newState
                removeSource(lightAppPermGroupLiveData)
            }
        }

    val appLabel: CharSequence
        get() = KotlinUtils.getPackageLabel(getApplication(), packageName, user)

    val locationPinIcon: Drawable
        get() =
            Utils.getGroupInfo(Manifest.permission_group.LOCATION, app)!!.loadIcon(
                app.packageManager
            )

    fun onAllow() {
        var appPermGroup = lightAppPermGroupLiveData.value!!
        val result =
            if (appPermGroup.isTrustedUiConsented)
                LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__DIRECT_GRANT
            else LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__PROMPT_GRANT
        val coarsePermission = appPermGroup.permissions[ACCESS_COARSE_LOCATION]!!
        // Do not save Trusted UI consent if coarse is permanently granted, because in
        // settings we don't show "... or allow when you share".
        val shouldSetTrustedUiFlag = !coarsePermission.isGranted || coarsePermission.isOneTime

        // Grant coarse permission, if not granted.
        if (!coarsePermission.isGranted) {
            appPermGroup =
                KotlinUtils.grantForegroundRuntimePermissions(
                    app,
                    group = appPermGroup,
                    filterPermissions = listOf(ACCESS_COARSE_LOCATION),
                    isOneTime = true,
                )
        }
        KotlinUtils.grantForegroundRuntimePermissions(
            app,
            group = appPermGroup,
            filterPermissions = listOf(ACCESS_FINE_LOCATION),
            isOneTime = true,
            isTrustedUi = shouldSetTrustedUiFlag,
        )

        val bundle =
            Bundle().apply { putBoolean(LocationButtonClient.EXTRA_PERMISSION_RESULT, true) }
        remoteCallback.sendResult(bundle)
        logLocationButtonRequestResult(result)
    }

    fun onDontAllow() {
        var appPermGroup = lightAppPermGroupLiveData.value!!
        val coarsePermission = appPermGroup.permissions[ACCESS_COARSE_LOCATION]!!
        // If coarse permission isn't granted, clear one-time flag to ensure app permission page
        // shows "don't allow" button selected.
        if (!coarsePermission.isGranted && coarsePermission.isOneTime) {
            appPermGroup =
                KotlinUtils.setGroupFlags(
                    app,
                    appPermGroup,
                    PackageManager.FLAG_PERMISSION_ONE_TIME to false,
                    filterPermissions = listOf(ACCESS_COARSE_LOCATION),
                )
        }
        val precisePermission = appPermGroup.permissions[ACCESS_FINE_LOCATION]!!
        KotlinUtils.revokeForegroundRuntimePermissions(
            app,
            appPermGroup,
            userFixed = precisePermission.isUserSet || precisePermission.isUserFixed,
            filterPermissions = listOf(ACCESS_FINE_LOCATION),
        )

        val bundle =
            Bundle().apply { putBoolean(LocationButtonClient.EXTRA_PERMISSION_RESULT, false) }
        remoteCallback.sendResult(bundle)
        logLocationButtonRequestResult(LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__DENIED)
    }

    fun onCancel() {
        logLocationButtonRequestResult(LOCATION_BUTTON_REQUEST_RESULT_REPORTED__RESULT__CANCELED)
    }

    // This code is derived from GrantPermissionsViewModel.isPermissionGrantableAndNotFixed()
    private fun isPermissionNotGrantable(lightAppPermGroup: LightAppPermGroup): Boolean {
        val precisePermission = lightAppPermGroup.permissions[ACCESS_FINE_LOCATION]
        if (precisePermission == null) {
            return true
        }
        val policyFixed =
            (lightAppPermGroup.foreground.isPolicyFixed && !lightAppPermGroup.isGranted) ||
                precisePermission.isPolicyFixed

        return policyFixed || lightAppPermGroup.foreground.isUserFixed
    }

    private fun logLocationButtonRequestResult(result: Int) {
        val lightAppPermGroup = lightAppPermGroupLiveData.value!!
        Log.i(
            LOG_TAG,
            "Location button permission grant result requestId=$sessionId " +
                "callingUid=${lightAppPermGroup.packageInfo.uid} " +
                "callingPackage=$packageName " +
                "permission=$ACCESS_FINE_LOCATION " +
                "result=$result ",
        )
        PermissionControllerStatsLog.write(
            LOCATION_BUTTON_REQUEST_RESULT_REPORTED,
            sessionId,
            lightAppPermGroup.packageInfo.uid,
            packageName,
            LOCATION_BUTTON_REQUEST_RESULT_REPORTED__PERMISSION__ACCESS_FINE_LOCATION,
            result,
        )
    }

    companion object {
        private const val LOG_TAG = "LocationButtonViewModel"
    }

    enum class LocationButtonRequestState {
        LOADING,
        ALREADY_GRANTED,
        NOT_GRANTABLE,
        AUTO_GRANTED,
        AUTO_DENIED,
        CONSENTED,
        SHOW_UI,
    }
}

/**
 * Factory for a [LocationButtonViewModel]
 *
 * @param packageName The name of the package this viewModel is representing
 * @param remoteCallback The callback to send grant result
 */
class LocationButtonViewModelFactory(
    private val application: Application,
    private val sessionId: Long,
    private val packageName: String,
    private val remoteCallback: RemoteCallback,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LocationButtonViewModel(application, sessionId, packageName, remoteCallback) as T
    }
}
