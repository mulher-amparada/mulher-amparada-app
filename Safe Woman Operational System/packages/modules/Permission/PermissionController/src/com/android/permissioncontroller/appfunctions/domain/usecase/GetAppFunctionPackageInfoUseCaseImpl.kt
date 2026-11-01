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
package com.android.permissioncontroller.appfunctions.domain.usecase

import android.content.Context
import android.os.UserHandle
import com.android.permissioncontroller.R
import com.android.permissioncontroller.appfunctions.data.repository.AppFunctionRepository
import com.android.permissioncontroller.appfunctions.domain.model.v31.AppFunctionPackageInfo
import com.android.permissioncontroller.appfunctions.domain.usecase.v31.GetAppFunctionPackageInfoUseCase
import com.android.permissioncontroller.pm.data.repository.v31.PackageRepository

/**
 * This use case returns a [AppFunctionPackageInfo] for the specified package and user
 *
 * @param packageRepository The repository to use to get the package labels and icons.
 */
class GetAppFunctionPackageInfoUseCaseImpl(private val packageRepository: PackageRepository) :
    GetAppFunctionPackageInfoUseCase {
    override operator fun invoke(
        packageName: String,
        context: Context,
        user: UserHandle,
    ): AppFunctionPackageInfo {
        val label =
            if (packageName == AppFunctionRepository.DEVICE_SETTINGS_TARGET_PACKAGE_NAME) {
                context.getString(R.string.app_function_device_settings_target_title)
            } else {
                packageRepository.getPackageLabel(packageName, user)
            }

        val icon =
            if (packageName == AppFunctionRepository.DEVICE_SETTINGS_TARGET_PACKAGE_NAME) {
                val settingsPackageName = packageRepository.getSettingsPackageName(user)
                settingsPackageName?.let {
                    packageRepository.getBadgedPackageIcon(settingsPackageName, user)
                } ?: context.getDrawable(R.drawable.ic_appfunction_target_device_settings)
            } else {
                packageRepository.getBadgedPackageIcon(packageName, user)
            }
        return AppFunctionPackageInfo(packageName, label, icon)
    }
}
