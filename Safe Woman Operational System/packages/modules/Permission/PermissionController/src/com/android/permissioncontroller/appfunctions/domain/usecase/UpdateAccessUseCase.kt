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

import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_OTHER_DENIED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_OTHER_GRANTED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_DENIED
import android.app.appfunctions.AppFunctionManager.ACCESS_FLAG_USER_GRANTED
import com.android.permissioncontroller.appfunctions.data.repository.AppFunctionRepository

/**
 * This use case updates access state for a specified agent and target packages.
 *
 * @param appFunctionRepository The repository to use to get the app function agents.
 */
class UpdateAccessUseCase(private val appFunctionRepository: AppFunctionRepository) {
    suspend operator fun invoke(
        agentPackageName: String,
        targetPackageName: String,
        isGranted: Boolean,
    ) {
        appFunctionRepository.updateAccessFlags(
            agentPackageName,
            targetPackageName,
            ACCESS_FLAG_USER_GRANTED or
                ACCESS_FLAG_USER_DENIED or
                ACCESS_FLAG_OTHER_GRANTED or
                ACCESS_FLAG_OTHER_DENIED,
            if (isGranted) ACCESS_FLAG_USER_GRANTED else ACCESS_FLAG_USER_DENIED,
        )
    }
}
