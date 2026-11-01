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

import android.content.Context
import androidx.annotation.RequiresApi
import com.android.onboarding.contracts.provisioning.CustomUserConsentDetails
import com.android.onboarding.contracts.provisioning.GetOemProvisioningCustomizationsContract
import com.android.onboarding.contracts.provisioning.GetOemProvisioningCustomizationsInput
import com.android.onboarding.contracts.provisioning.OemProvisioningCustomizations
import com.android.onboarding.tasks.OnboardingTask

/** Task that returns [OemProvisioningCustomizations].
 *
 * OEMs who wish to show custom terms during provisioning should modify the [runTask] method of
 * this class to return their own [CustomUserConsentRequired][CustomUserConsentDetails.CustomUserConsentRequired]
 */
@RequiresApi(37)
class GetOemProvisioningCustomizationsTask : OnboardingTask<
        GetOemProvisioningCustomizationsInput,
        OemProvisioningCustomizations,
        GetOemProvisioningCustomizationsContract,
        >() {
    override val contract = GetOemProvisioningCustomizationsContract
    override suspend fun runTask(
        context: Context, input: GetOemProvisioningCustomizationsInput
    ): OemProvisioningCustomizations =
        OemProvisioningCustomizations(CustomUserConsentDetails.CustomUserConsentNotRequired)
}