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
package com.android.permissioncontroller.appfunctions.domain.usecase.v31

import android.content.Context
import android.os.UserHandle
import com.android.permissioncontroller.appfunctions.domain.model.v31.AppFunctionPackageInfo

/** A use case interface for retrieving [AppFunctionPackageInfo] for a given package and user. */
interface GetAppFunctionPackageInfoUseCase {
    /**
     * Retrieves the [AppFunctionPackageInfo] for a specific package and user.
     *
     * @param packageName The name of the package.
     * @param context The [Context] to use for querying package information.
     * @param user The [UserHandle] of the user.
     */
    operator fun invoke(
        packageName: String,
        context: Context,
        user: UserHandle,
    ): AppFunctionPackageInfo?
}
