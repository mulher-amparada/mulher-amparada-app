/*
 * Copyright (C) 2019 The Android Open Source Project
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
@file:Suppress("DEPRECATION")

package com.android.permissioncontroller.permission.data

import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_ATTRIBUTIONS
import android.content.pm.PackageManager.GET_ATTRIBUTIONS_LONG
import android.content.pm.PackageManager.GET_PERMISSIONS
import android.content.pm.PackageManager.MATCH_ALL
import android.os.UserHandle
import com.android.modules.utils.build.SdkLevel
import com.android.permissioncontroller.PermissionControllerApplication
import com.android.permissioncontroller.permission.model.livedatatypes.LightPackageInfo
import com.android.permissioncontroller.permission.utils.ContextCompat
import com.android.permissioncontroller.permission.utils.v35.MultiDeviceUtils
import kotlinx.coroutines.Job

/**
 * A LiveData which tracks all of the packageinfos installed for a given user.
 *
 * @param app The current application
 * @param user The user whose packages are desired
 */
class UserPackageInfosLiveData
private constructor(
    private val app: Application,
    private val user: UserHandle,
    private val deviceId: Int,
) :
    SmartAsyncMediatorLiveData<@JvmSuppressWildcards List<LightPackageInfo>>(),
    PackageBroadcastReceiver.PackageBroadcastListener,
    PermissionListenerMultiplexer.PermissionChangeCallback {

    /** Whether or not the permissions in this liveData are out of date */
    var permChangeStale = false

    override fun onPackageUpdate(packageName: String) {
        updateAsync()
    }

    override fun onPermissionChange() {
        permChangeStale = true
        for (packageInfo in value ?: emptyList()) {
            PermissionListenerMultiplexer.removeCallback(packageInfo.uid, this)
        }
    }

    override fun setValue(newValue: List<LightPackageInfo>?) {
        if (newValue != value) {
            for (packageInfo in value ?: emptyList()) {
                PermissionListenerMultiplexer.removeCallback(packageInfo.uid, this)
            }
            for (packageInfo in newValue ?: emptyList()) {
                PermissionListenerMultiplexer.addCallback(packageInfo.uid, this)
            }
        }
        super.setValue(newValue)
        permChangeStale = false
    }

    /** Get all of the packages in the system, organized by user. */
    override suspend fun loadDataAndPostValue(job: Job) {
        if (job.isCancelled) {
            return
        }

        val packageInfos =
            if (SdkLevel.isAtLeastU()) {
                app.applicationContext.packageManager.getInstalledPackagesAsUser(
                    PackageManager.PackageInfoFlags.of(
                        GET_PERMISSIONS.toLong() or GET_ATTRIBUTIONS_LONG or MATCH_ALL.toLong()
                    ),
                    user.identifier,
                )
            } else if (SdkLevel.isAtLeastS()) {
                app.applicationContext.packageManager.getInstalledPackagesAsUser(
                    GET_PERMISSIONS or GET_ATTRIBUTIONS or MATCH_ALL,
                    user.identifier,
                )
            } else {
                app.applicationContext.packageManager.getInstalledPackagesAsUser(
                    GET_PERMISSIONS or MATCH_ALL,
                    user.identifier,
                )
            }

        val lightPackageInfos =
            packageInfos.map { packageInfo ->
                // PackageInfo#requestedPermissionsFlags is not device aware. Hence for
                // device aware permissions if the deviceId is not the primary device we
                // need to separately check permission for that device and update
                // requestedPermissionsFlags.
                if (SdkLevel.isAtLeastV() && deviceId != ContextCompat.DEVICE_ID_DEFAULT) {
                    val requestedPermissionsFlagsForDevice =
                        MultiDeviceUtils.getPermissionsFlagsForDevice(
                            app,
                            packageInfo.requestedPermissions?.toList() ?: emptyList(),
                            packageInfo.requestedPermissionsFlags?.toList() ?: emptyList(),
                            packageInfo.applicationInfo!!.uid,
                            deviceId,
                        )

                    LightPackageInfo(packageInfo, deviceId, requestedPermissionsFlagsForDevice)
                } else {
                    LightPackageInfo(packageInfo)
                }
            }

        postValue(lightPackageInfos)
    }

    override fun onActive() {
        super.onActive()

        PackageBroadcastReceiver.addAllCallback(this)

        for (packageInfo in value ?: emptyList()) {
            PermissionListenerMultiplexer.addCallback(packageInfo.uid, this)
        }
    }

    override fun onInactive() {
        super.onInactive()

        for (packageInfo in value ?: emptyList()) {
            PermissionListenerMultiplexer.removeCallback(packageInfo.uid, this)
        }

        PackageBroadcastReceiver.removeAllCallback(this)
    }

    /**
     * Repository for UserPackageInfosLiveDatas.
     *
     * <p> Key value is a UserHandle, value is its corresponding LiveData.
     */
    companion object : DataRepositoryForDevice<UserHandle, UserPackageInfosLiveData>() {
        override fun newValue(key: UserHandle, deviceId: Int): UserPackageInfosLiveData {
            return UserPackageInfosLiveData(PermissionControllerApplication.get(), key, deviceId)
        }
    }
}
