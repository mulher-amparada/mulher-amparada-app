/*
 * Copyright 2025 The Android Open Source Project
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

package com.android.permissioncontroller.permission.service

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.KeyguardManager
import android.app.appfunctions.AppFunctionException
import android.app.appfunctions.AppFunctionException.ERROR_DENIED
import android.app.appfunctions.AppFunctionException.ERROR_FUNCTION_NOT_FOUND
import android.app.appfunctions.ExecuteAppFunctionRequest
import android.app.appfunctions.ExecuteAppFunctionResponse
import android.app.appsearch.GenericDocument
import android.app.usage.UsageStats
import android.content.Context
import android.content.pm.SigningInfo
import android.content.res.Configuration
import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import android.os.UserHandle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.android.permissioncontroller.appfunctions.AdditionalPermissionsScreen
import com.android.permissioncontroller.appfunctions.AppPermissionGrantStateScreen
import com.android.permissioncontroller.appfunctions.AppPermissionSettingScreen
import com.android.permissioncontroller.appfunctions.AppPermissionsScreen
import com.android.permissioncontroller.appfunctions.DefaultAppListScreen
import com.android.permissioncontroller.appfunctions.DefaultAppScreen
import com.android.permissioncontroller.appfunctions.GenericDocumentToPlatformConverter
import com.android.permissioncontroller.appfunctions.PermissionAppsScreen
import com.android.permissioncontroller.appfunctions.PermissionManagerScreen
import com.android.permissioncontroller.appfunctions.UnusedAppLastUsageScreen
import com.android.permissioncontroller.appfunctions.UnusedAppsScreen
import com.android.permissioncontroller.appfunctions.deviceStateScreenKeys
import com.android.permissioncontroller.hibernation.lastTimePackageUsed
import com.android.permissioncontroller.permission.data.AllPackageInfosLiveData
import com.android.permissioncontroller.permission.data.SinglePermGroupPackagesUiInfoLiveData
import com.android.permissioncontroller.permission.data.UsageStatsLiveData
import com.android.permissioncontroller.permission.data.getUnusedPackages
import com.android.permissioncontroller.permission.model.livedatatypes.LightPackageInfo
import com.android.permissioncontroller.permission.model.v31.AppPermissionUsage
import com.android.permissioncontroller.permission.model.v31.PermissionUsages
import com.android.permissioncontroller.permission.model.v31.PermissionUsages.PermissionsUsagesChangeCallback
import com.android.permissioncontroller.permission.utils.getInitializedValue
import com.android.permissioncontroller.role.ui.DefaultAppListViewModel
import com.android.permissioncontroller.role.ui.DefaultAppViewModel
import com.android.permissioncontroller.role.ui.RoleItem
import com.google.android.appfunctions.schema.common.v1.devicestate.DeviceStateResponse
import com.google.android.appfunctions.schema.common.v1.devicestate.PerScreenDeviceStates
import java.time.Instant
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// TODO b/411150350: Add CTS test for this service
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
class DeviceStateAppFunctionService :
    LifecycleAppFunctionService(), ViewModelStoreOwner, PermissionsUsagesChangeCallback {

    private lateinit var englishContext: Context
    private lateinit var permissionUsages: PermissionUsages

    override val viewModelStore by lazy { ViewModelStore() }

    override fun onCreate() {
        super.onCreate()
        englishContext = createEnglishContext()
        permissionUsages = PermissionUsages(this)
    }

    override fun onExecuteFunction(
        request: ExecuteAppFunctionRequest,
        callingPackage: String,
        callingPackageSigningInfo: SigningInfo,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<ExecuteAppFunctionResponse, AppFunctionException>,
    ) {
        super.onExecuteFunction(
            request,
            callingPackage,
            callingPackageSigningInfo,
            cancellationSignal,
            callback,
        )

        if (request.functionIdentifier != APP_FUNCTION_IDENTIFIER) {
            callback.onError(
                AppFunctionException(
                    ERROR_FUNCTION_NOT_FOUND,
                    "${request.functionIdentifier} not supported.",
                )
            )
            return
        }

        if (
            shouldCheckForDeviceLock(request.parameters) &&
                applicationContext.getSystemService(KeyguardManager::class.java).isDeviceLocked
        ) {
            callback.onError(
                AppFunctionException(
                    ERROR_DENIED,
                    "Attempting to execute a device state app function while " +
                        "the device is locked.",
                )
            )
        }

        lifecycleScope.launch {
            val jetpackDocument =
                androidx.appsearch.app.GenericDocument.fromDocumentClass(buildDeviceStateResponse())

            val platformDocument =
                GenericDocumentToPlatformConverter.toPlatformGenericDocument(jetpackDocument)

            val resultDocument =
                GenericDocument.Builder<GenericDocument.Builder<*>>("", "", "")
                    .setPropertyDocument(
                        ExecuteAppFunctionResponse.PROPERTY_RETURN_VALUE,
                        platformDocument,
                    )
                    .build()
            val response = ExecuteAppFunctionResponse(resultDocument)
            callback.onResult(response)
        }
    }

    private fun createEnglishContext(): Context {
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(Locale.US)
        return createConfigurationContext(configuration)
    }

    private fun shouldCheckForDeviceLock(params: GenericDocument): Boolean {
        return params
            .getPropertyDocument(APP_FUNCTION_PARAMS_KEY)
            ?.getPropertyBoolean(REQUEST_INITIATED_WHILE_UNLOCKED_KEY) != true
    }

    private suspend fun buildDeviceStateResponse(): DeviceStateResponse {
        val startTime = System.currentTimeMillis()
        val perScreenDeviceStatesList = coroutineScope {
            deviceStateScreenKeys.map { async { buildPerScreenDeviceStates(it) } }.awaitAll()
        }
        val locale = resources.configuration.locales[0]

        if (DEBUG) {
            Log.i(
                TAG,
                "Total time spent to fetch data = ${System.currentTimeMillis() - startTime} ms",
            )
        }
        return DeviceStateResponse(
            perScreenDeviceStates = perScreenDeviceStatesList.flatten(),
            deviceLocale = locale.toString(),
        )
    }

    private suspend fun buildPerScreenDeviceStates(screenKey: String): List<PerScreenDeviceStates> {
        val startTime = System.currentTimeMillis()

        when (screenKey) {
            PermissionManagerScreen.KEY -> {
                return listOf(PermissionManagerScreen(this).toPerScreenDeviceStates())
            }
            PermissionAppsScreen.KEY -> {
                return coroutineScope {
                    SUPPORTED_PERMISSION_GROUPS.map {
                            async {
                                PermissionAppsScreen(this@DeviceStateAppFunctionService, it)
                                    .toPerScreenDeviceStates()
                            }
                        }
                        .awaitAll()
                }
            }
            AppPermissionsScreen.KEY -> {
                return listOf(AppPermissionsScreen(this).toPerScreenDeviceStates())
            }
            AppPermissionSettingScreen.KEY -> {
                val result =
                    SUPPORTED_PERMISSION_GROUPS.map { permissionGroup ->
                        AppPermissionSettingScreen(
                                this@DeviceStateAppFunctionService,
                                permissionGroup,
                            )
                            .toPerScreenDeviceStates()
                    }

                return result
            }
            AppPermissionGrantStateScreen.KEY -> {
                val filterBeginTimeMillis =
                    System.currentTimeMillis() -
                        TimeUnit.DAYS.toMillis(PERMISSION_USAGE_START_DAY_FROM_NOW)

                permissionUsages.load(
                    null,
                    SUPPORTED_PERMISSION_GROUPS.toTypedArray(),
                    filterBeginTimeMillis,
                    Long.MAX_VALUE,
                    PermissionUsages.USAGE_FLAG_LAST,
                    null,
                    false,
                    false,
                    this,
                    true,
                )

                val appPermissionUsages = permissionUsages.usages
                if (DEBUG) {
                    Log.i(
                        TAG,
                        "Time spent on fetching permission usages = ${System.currentTimeMillis() - startTime} ms",
                    )
                }

                val result = coroutineScope {
                    SUPPORTED_PERMISSION_GROUPS.map { permissionGroup ->
                            async {
                                val packagePermissionInfoMap =
                                    SinglePermGroupPackagesUiInfoLiveData[permissionGroup]
                                        .getInitializedValue(staleOk = false, forceUpdate = true)!!

                                val deviceStateScreens = mutableListOf<PerScreenDeviceStates>()
                                packagePermissionInfoMap.forEach { (packageInfo, permissionInfo) ->
                                    deviceStateScreens.add(
                                        AppPermissionGrantStateScreen(
                                                context = this@DeviceStateAppFunctionService,
                                                permissionGroup = permissionGroup,
                                                packageName = packageInfo.first,
                                                userHandle = packageInfo.second,
                                                permissionGrantState =
                                                    permissionInfo.permGrantState,
                                                lastAccessTime =
                                                    extractLastAccessTime(
                                                        appPermissionUsages,
                                                        permissionGroup,
                                                        packageInfo.first,
                                                        packageInfo.second,
                                                    ),
                                                usePreciseLocation =
                                                    checkUsePreciseLocation(
                                                        appPermissionUsages,
                                                        packageInfo.first,
                                                        permissionGroup,
                                                    ),
                                            )
                                            .toPerScreenDeviceStates()
                                    )
                                }
                                deviceStateScreens
                            }
                        }
                        .awaitAll()
                        .flatten()
                }
                if (DEBUG) {
                    Log.i(
                        TAG,
                        "Time spent on ${AppPermissionGrantStateScreen.KEY} = ${System.currentTimeMillis() - startTime} ms",
                    )
                }

                return result
            }
            UnusedAppsScreen.KEY -> {
                return listOf(UnusedAppsScreen(this).toPerScreenDeviceStates())
            }
            UnusedAppLastUsageScreen.KEY -> {
                val unusedApps = getUnusedPackages().getInitializedValue()!!
                val usageStats =
                    UsageStatsLiveData[MAX_UNUSED_PERIOD_MILLIS].getInitializedValue() ?: emptyMap()
                val allPackageInfos = AllPackageInfosLiveData.getInitializedValue()!!
                val lastUsedDataUnusedApps =
                    extractUnusedAppsUsageData(usageStats, unusedApps) { it: UsageStats ->
                        PackageLastUsageTime(it.packageName, it.lastTimePackageUsed())
                    }
                val firstInstallDataUnusedApps =
                    extractUnusedAppsUsageData(allPackageInfos, unusedApps) { it: LightPackageInfo
                        ->
                        PackageLastUsageTime(it.packageName, it.firstInstallTime)
                    }

                val deviceStateScreens = mutableListOf<PerScreenDeviceStates>()
                unusedApps.keys.forEach { (packageName, user) ->
                    val userPackage = packageName to user
                    val lastUsageTime =
                        lastUsedDataUnusedApps[userPackage]
                            ?: firstInstallDataUnusedApps[userPackage]
                            ?: 0L

                    deviceStateScreens.add(
                        UnusedAppLastUsageScreen(
                                context = this,
                                packageName = packageName,
                                lastUsageTime = lastUsageTime,
                            )
                            .toPerScreenDeviceStates()
                    )
                }

                if (DEBUG) {
                    Log.i(
                        TAG,
                        "Time spent on ${UnusedAppLastUsageScreen.KEY} = ${System.currentTimeMillis() - startTime} ms",
                    )
                }

                return deviceStateScreens
            }
            AdditionalPermissionsScreen.KEY -> {
                return listOf(AdditionalPermissionsScreen(this).toPerScreenDeviceStates())
            }
            DefaultAppListScreen.KEY -> {
                return listOf(DefaultAppListScreen(this).toPerScreenDeviceStates())
            }
            DefaultAppScreen.KEY -> {
                val viewModelFactory: ViewModelProvider.Factory =
                    ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                val viewModel =
                    ViewModelProvider(this, viewModelFactory)[DefaultAppListViewModel::class.java]
                val deviceStateScreens = mutableListOf<PerScreenDeviceStates>()

                coroutineScope {
                    val roleItems = viewModel.liveData.getInitializedValue()
                    val user = viewModel.user
                    roleItems
                        ?.map { roleItem ->
                            async { getDefaultAppDeviceStateScreen(roleItem, false, user) }
                        }
                        ?.awaitAll()
                        ?.filterNotNull()
                        ?.also { deviceStateScreens.addAll(it) }

                    if (viewModel.hasWorkProfile() && viewModel.workLiveData != null) {
                        val workProfileRoleItems = viewModel.workLiveData!!.getInitializedValue()
                        val workProfile = viewModel.workProfile!!
                        workProfileRoleItems
                            ?.map { roleItem ->
                                async {
                                    getDefaultAppDeviceStateScreen(roleItem, true, workProfile)
                                }
                            }
                            ?.awaitAll()
                            ?.filterNotNull()
                            ?.also { deviceStateScreens.addAll(it) }
                    }
                }

                if (DEBUG) {
                    Log.i(
                        TAG,
                        "Time spent on ${DefaultAppScreen.KEY} = ${System.currentTimeMillis() - startTime} ms",
                    )
                }

                return deviceStateScreens
            }
        }
        throw Exception("$screenKey is not supported")
    }

    override fun onPermissionUsagesChanged() {}

    private suspend fun getDefaultAppDeviceStateScreen(
        roleItem: RoleItem,
        isWorkProfile: Boolean,
        user: UserHandle,
    ): PerScreenDeviceStates? {
        //  Create a unique key for this specific ViewModel instance.
        //  This ensures we get a different ViewModel for each role and user combination.
        val viewModelKey = "DefaultAppViewModel_${roleItem.role.name}_${user.identifier}"
        val provider =
            ViewModelProvider(this, DefaultAppViewModel.Factory(roleItem.role, user, application))
        val defaultAppViewModel = provider[viewModelKey, DefaultAppViewModel::class.java]

        return defaultAppViewModel.liveData.getInitializedValue()?.let { items ->
            DefaultAppScreen(this, roleItem.role, items, isWorkProfile, user)
                .toPerScreenDeviceStates()
        }
    }

    /**
     * Extract PackageLastUsageTime for unused apps from userPackages map. This method may be used
     * for extracting different usage time (such as installation time or last opened time) from
     * different Package structures
     */
    private fun <PackageData> extractUnusedAppsUsageData(
        usageStats: Map<UserHandle, List<PackageData>>,
        unusedApps: Map<Pair<String, UserHandle>, Set<String>>,
        extractUsageData: (fullData: PackageData) -> PackageLastUsageTime,
    ): Map<Pair<String, UserHandle>, Long> {
        return usageStats
            .flatMap { (userHandle, fullData) ->
                fullData.map { userHandle to extractUsageData(it) }
            }
            .associate { (handle, appData) -> (appData.packageName to handle) to appData.usageTime }
            .filterKeys { unusedApps.contains(it) }
    }

    private fun extractLastAccessTime(
        appPermissionUsages: List<AppPermissionUsage>,
        permissionGroup: String,
        packageName: String,
        userHandle: UserHandle,
    ): Long {
        val filterTimeBeginMillis =
            max(
                System.currentTimeMillis() -
                    TimeUnit.DAYS.toMillis(PERMISSION_USAGE_START_DAY_FROM_NOW),
                Instant.EPOCH.toEpochMilli(),
            )
        for (appUsage in appPermissionUsages) {
            if (packageName != appUsage.packageName) {
                continue
            }
            for (groupUsage in appUsage.groupUsages) {
                if (
                    permissionGroup != groupUsage.group.name || userHandle != groupUsage.group.user
                ) {
                    continue
                }
                if (groupUsage.lastAccessTime >= filterTimeBeginMillis) {
                    return groupUsage.lastAccessTime
                }
            }
        }
        return -1
    }

    /**
     * The returned value corresponds to the Use precise location UI on the app permission page
     * - null: Use precise location doesn't apply to the current permission group due to it is not a
     *   location permission or the permission is not granted.
     * - true: Use precise location is enabled
     * - false: Use precise location is disabled
     */
    private fun checkUsePreciseLocation(
        appPermissionUsages: List<AppPermissionUsage>,
        packageName: String,
        permissionGroup: String,
    ): Boolean? {
        if (permissionGroup != Manifest.permission_group.LOCATION) {
            return null
        }

        for (appUsage in appPermissionUsages) {
            if (packageName != appUsage.packageName) {
                continue
            }
            for (groupUsage in appUsage.groupUsages) {
                val group = groupUsage.group
                if (group.name != Manifest.permission_group.LOCATION) {
                    continue
                }
                val coarseLocation = group.getPermission(ACCESS_COARSE_LOCATION)
                val fineLocation = group.getPermission(ACCESS_FINE_LOCATION)

                if (
                    coarseLocation == null ||
                        fineLocation == null ||
                        !group.areRuntimePermissionsGranted() ||
                        group.isOneTime
                ) {
                    return null
                }

                // Steps to decide location accuracy toggle state
                // 1. If FINE or COARSE are granted, then return true if FINE is granted.
                // 2. Else if FINE or COARSE have the isSelectedLocationAccuracy flag set, then
                // return
                //    true if FINE isSelectedLocationAccuracy is set.
                // 3. Else, return default precision from device config.
                return if (fineLocation.isGranted || coarseLocation.isGranted) {
                    fineLocation.isGranted
                } else if (
                    fineLocation.isSelectedLocationAccuracy ||
                        coarseLocation.isSelectedLocationAccuracy
                ) {
                    fineLocation.isSelectedLocationAccuracy
                } else {
                    // default location precision is true, indicates FINE
                    true
                }
            }
        }
        return false
    }

    private data class PackageLastUsageTime(val packageName: String, val usageTime: Long)

    companion object {
        private const val TAG = "DeviceStateService"
        private const val APP_FUNCTION_IDENTIFIER = "getPermissionsDeviceState"
        private const val APP_FUNCTION_PARAMS_KEY = "getPermissionsDeviceStateParams"
        private const val REQUEST_INITIATED_WHILE_UNLOCKED_KEY = "requestInitiatedWhileUnlocked"
        private const val DEBUG = false
        private val SUPPORTED_PERMISSION_GROUPS =
            setOf(
                Manifest.permission_group.LOCATION,
                Manifest.permission_group.CONTACTS,
                Manifest.permission_group.CALL_LOG,
            )
        // These two constants are copied from PermissionUsages and UnusedAppsViewModel respectively
        // TODO: b/415813567 - use the same view model to reduce code duplicate
        private const val PERMISSION_USAGE_START_DAY_FROM_NOW: Long = 7
        private val MAX_UNUSED_PERIOD_MILLIS = 180.days.inWholeMilliseconds
    }
}
