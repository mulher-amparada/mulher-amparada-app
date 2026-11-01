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

package com.android.permissioncontroller.pm.data.repository.v31

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import com.android.modules.utils.build.SdkLevel
import com.android.permissioncontroller.permission.utils.KotlinUtils
import com.android.permissioncontroller.permission.utils.Utils
import com.android.permissioncontroller.pm.data.model.v31.PackageAttributionModel
import com.android.permissioncontroller.pm.data.model.v31.PackageInfoModel
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to access package info data exposed by [PackageManager]. Domain and view layer
 * shouldn't access [PackageManager] directly, instead they should use the repository.
 */
interface PackageRepository {
    /**
     * Returns an integer UID who owns the given package name
     *
     * @see PackageManager.getPackageUid
     */
    fun getPackageUid(packageName: String, user: UserHandle): Int

    /**
     * Returns a package label for the given [packageName] and [user] Returns [packageName] if the
     * package is not found.
     */
    fun getPackageLabel(packageName: String, user: UserHandle): String

    /**
     * Returns a package's badged icon for the given [packageName] and [user] Returns null if the
     * package is not found.
     */
    fun getBadgedPackageIcon(packageName: String, user: UserHandle): Drawable?

    /**
     * Returns a [PackageInfoModel] for the given [packageName] and [user] Returns null if the
     * package is not found.
     */
    suspend fun getPackageInfo(
        packageName: String,
        user: UserHandle,
        flags: Int = PackageManager.GET_PERMISSIONS,
    ): PackageInfoModel?

    /**
     * Returns a [PackageAttributionModel] for the given [packageName] and [user] Returns null if
     * the package is not found.
     */
    suspend fun getPackageAttributionInfo(
        packageName: String,
        user: UserHandle,
    ): PackageAttributionModel?

    /** Returns the package name for the Settings app of the given [user], null otherwise. */
    fun getSettingsPackageName(user: UserHandle): String?

    /** Returns a list of packages holding permissions specified */
    fun getPackagesHoldingPermissions(permissions: List<String>, user: UserHandle): List<String>

    companion object {
        @Volatile private var instance: PackageRepository? = null

        fun getInstance(app: Application): PackageRepository =
            instance ?: synchronized(this) { PackageRepositoryImpl(app).also { instance = it } }

        fun createInstance(context: Context): PackageRepository = PackageRepositoryImpl(context)
    }
}

class PackageRepositoryImpl(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PackageRepository {
    override fun getPackageUid(packageName: String, user: UserHandle): Int {
        return try {
            val userContext = Utils.getUserContext(context, user)
            userContext.packageManager.getPackageUid(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            android.os.Process.INVALID_UID
        }
    }

    override fun getPackageLabel(packageName: String, user: UserHandle): String {
        return try {
            val userContext = Utils.getUserContext(context, user)
            val appInfo = userContext.packageManager.getApplicationInfo(packageName, 0)
            Utils.getFullAppLabel(appInfo, context)
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    override fun getBadgedPackageIcon(packageName: String, user: UserHandle): Drawable? {
        return try {
            val userContext = Utils.getUserContext(context, user)
            val appInfo = userContext.packageManager.getApplicationInfo(packageName, 0)
            Utils.getBadgedIcon(context, appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override suspend fun getPackageInfo(
        packageName: String,
        user: UserHandle,
        flags: Int,
    ): PackageInfoModel? =
        withContext(dispatcher) {
            try {
                val packageInfo =
                    Utils.getUserContext(context, user)
                        .packageManager
                        .getPackageInfo(packageName, flags)
                PackageInfoModel(packageInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(LOG_TAG, "package $packageName not found for user ${user.identifier}")
                null
            }
        }

    @Suppress("DEPRECATION")
    override suspend fun getPackageAttributionInfo(
        packageName: String,
        user: UserHandle,
    ): PackageAttributionModel? =
        withContext(dispatcher) {
            try {
                val packageInfo =
                    if (SdkLevel.isAtLeastU()) {
                        Utils.getUserContext(context, user)
                            .packageManager
                            .getPackageInfo(
                                packageName,
                                PackageInfoFlags.of(PackageManager.GET_ATTRIBUTIONS_LONG),
                            )
                    } else {
                        Utils.getUserContext(context, user)
                            .packageManager
                            .getPackageInfo(packageName, PackageManager.GET_ATTRIBUTIONS)
                    }
                val attributionUserVisible =
                    packageInfo.applicationInfo?.areAttributionsUserVisible() ?: false
                if (attributionUserVisible && SdkLevel.isAtLeastS()) {
                    val pkgContext = context.createPackageContext(packageName, 0)
                    val attributionTagToLabelRes =
                        packageInfo.attributions?.associate { it.tag to it.label }
                    val labelResToLabelStringMap =
                        attributionTagToLabelRes
                            ?.mapNotNull { entry ->
                                val labelString =
                                    try {
                                        pkgContext.getString(entry.value)
                                    } catch (e: Resources.NotFoundException) {
                                        null
                                    }
                                if (labelString != null) entry.value to labelString else null
                            }
                            ?.toMap()
                    PackageAttributionModel(
                        packageName,
                        true,
                        attributionTagToLabelRes,
                        labelResToLabelStringMap,
                    )
                } else {
                    PackageAttributionModel(packageName)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(LOG_TAG, "package $packageName not found for user ${user.identifier}")
                null
            }
        }

    override fun getSettingsPackageName(user: UserHandle): String? =
        try {
            val userContext = Utils.getUserContext(context, user)
            KotlinUtils.getPackageNameForIntent(
                userContext.packageManager,
                Settings.ACTION_SETTINGS,
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

    override fun getPackagesHoldingPermissions(
        permissions: List<String>,
        user: UserHandle,
    ): List<String> {
        val pm = Utils.getUserContext(context, user).packageManager
        return pm.getPackagesHoldingPermissions(permissions.toTypedArray(), 0).map {
            it.packageName
        }
    }

    companion object {
        private const val LOG_TAG = "PackageRepository"
    }
}
