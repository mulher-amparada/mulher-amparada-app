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

package com.android.managedprovisioning.onboarding

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.android.onboarding.common.MANAGED_PROVISIONING_REF
import com.android.onboarding.contracts.provisioning.GetOemProvisioningCustomizationsContract
import com.android.onboarding.process.NoOpKeepAliveManager
import com.android.onboarding.utils.LazyContextualBuilder
import com.android.onboarding.tasks.OnboardingTaskManagerServiceBinder
import java.util.function.Supplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A Service that triggers the tasks required for enterprise provisioning flows.
 */
class ManagedProvisioningOnboardingTaskService : Service() {
override fun onBind(intent: Intent): IBinder = binder(applicationContext)

companion object {
    private val binder = LazyContextualBuilder { context ->
        OnboardingTaskManagerServiceBinder(
            component = MANAGED_PROVISIONING_REF,
            contextProvider = { context },
            taskPermissionManager = ManagedProvisioningOnboardingTaskPermissionManager,
            coroutineScope = CoroutineScope(Dispatchers.Default),
            keepAliveManager = NoOpKeepAliveManager { context },
            tasks = setOf(Supplier { GetOemProvisioningCustomizationsTask() }),
            providedTasks = setOf(GetOemProvisioningCustomizationsContract),
        )
    }
    }
}
