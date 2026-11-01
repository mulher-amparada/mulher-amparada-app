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
package com.android.healthconnect.controller.permissions.data

import androidx.annotation.StringRes
import com.android.healthconnect.controller.R

/** Represents the display strings used for Additional Permissions. */
data class AdditionalPermissionStrings(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val descriptionFallback: Int = 0,
)

fun additionalPermissionString(
    additionalPermission: HealthPermission.AdditionalPermission,
    type: AccessType,
    hasMedicalPermissions: Boolean,
    isMedicalReadGranted: Boolean,
    isFitnessReadGranted: Boolean,
): AdditionalPermissionStrings {
    return if (additionalPermission.isBackgroundReadPermission()) {
        getBackgroundReadString(
            type,
            hasMedicalPermissions,
            isMedicalReadGranted,
            isFitnessReadGranted,
        )
    } else if (additionalPermission.isHistoryReadPermission()) {
        getHistoryReadString(type, hasMedicalPermissions)
    } else {
        throw IllegalArgumentException("No strings for additional permission $additionalPermission")
    }
}

private fun getHistoryReadString(
    type: AccessType,
    hasMedicalPermissions: Boolean,
): AdditionalPermissionStrings {
    return if (hasMedicalPermissions) {
        getHistoryMedicalString(type)
    } else {
        getHistoryNonMedicalString(type)
    }
}

private fun getHistoryNonMedicalString(type: AccessType): AdditionalPermissionStrings {
    return when (type) {
        AccessType.SINGLE_REQUEST -> {
            AdditionalPermissionStrings(
                title = R.string.history_read_single_request_title,
                description = R.string.history_read_single_request_description,
                descriptionFallback = R.string.history_read_single_request_description_fallback,
            )
        }
        AccessType.COMBINED_REQUEST -> {
            AdditionalPermissionStrings(
                title = R.string.history_read_combined_request_title,
                description = R.string.history_read_combined_request_description,
                descriptionFallback = R.string.history_read_combined_request_description_fallback,
            )
        }
        AccessType.ACCESS -> {
            AdditionalPermissionStrings(
                title = R.string.history_read_access_title,
                description = R.string.history_read_access_description,
                descriptionFallback = R.string.history_read_access_description_fallback,
            )
        }
    }
}

private fun getHistoryMedicalString(type: AccessType): AdditionalPermissionStrings {
    return when (type) {
        AccessType.SINGLE_REQUEST -> {
            AdditionalPermissionStrings(
                title = R.string.history_read_single_request_title,
                description = R.string.history_read_medical_single_request_description,
                descriptionFallback =
                    R.string.history_read_medical_single_request_description_fallback,
            )
        }
        AccessType.COMBINED_REQUEST -> {
            AdditionalPermissionStrings(
                title = R.string.history_read_medical_combined_request_title,
                description = R.string.history_read_medical_combined_request_description,
                descriptionFallback =
                    R.string.history_read_medical_combined_request_description_fallback,
            )
        }
        AccessType.ACCESS -> {
            AdditionalPermissionStrings(
                title = R.string.history_read_medical_access_title,
                description = R.string.history_read_medical_access_description,
                descriptionFallback = R.string.history_read_medical_access_description_fallback,
            )
        }
    }
}

private fun getBackgroundReadString(
    type: AccessType,
    hasMedicalPermissions: Boolean,
    isMedicalReadGranted: Boolean,
    isFitnessReadGranted: Boolean,
): AdditionalPermissionStrings {
    return if (hasMedicalPermissions) {
        getBackgroundMedicalString(type, isMedicalReadGranted, isFitnessReadGranted)
    } else {
        getBackgroundNonMedicalString(type)
    }
}

private fun getBackgroundNonMedicalString(type: AccessType): AdditionalPermissionStrings {
    return when (type) {
        AccessType.SINGLE_REQUEST -> {
            AdditionalPermissionStrings(
                title = R.string.background_read_single_request_title,
                description = R.string.background_read_single_request_description,
            )
        }
        AccessType.COMBINED_REQUEST -> {
            AdditionalPermissionStrings(
                title = R.string.background_read_combined_request_title,
                description = R.string.background_read_combined_request_description,
            )
        }
        AccessType.ACCESS -> {
            AdditionalPermissionStrings(
                title = R.string.background_read_access_title,
                description = R.string.background_read_access_description,
            )
        }
    }
}

private fun getBackgroundMedicalString(
    type: AccessType,
    isMedicalReadGranted: Boolean,
    isFitnessReadGranted: Boolean,
): AdditionalPermissionStrings {
    return when (type) {
        AccessType.SINGLE_REQUEST -> {
            if (isMedicalReadGranted && isFitnessReadGranted) {
                AdditionalPermissionStrings(
                    title = R.string.background_read_single_request_title,
                    description =
                        R.string
                            .background_read_medical_single_request_description_both_types_granted,
                )
            } else if (isMedicalReadGranted) {
                AdditionalPermissionStrings(
                    title = R.string.background_read_single_request_title,
                    description =
                        R.string.background_read_medical_single_request_description_medical_granted,
                )
            } else if (isFitnessReadGranted) {
                AdditionalPermissionStrings(
                    title = R.string.background_read_single_request_title,
                    description =
                        R.string.background_read_medical_single_request_description_fitness_granted,
                )
            } else {
                throw IllegalArgumentException(
                    "No strings for this state of additional permission " +
                        "${HealthPermission.AdditionalPermission.READ_HEALTH_DATA_IN_BACKGROUND}"
                )
            }
        }
        AccessType.COMBINED_REQUEST -> {
            if (isMedicalReadGranted && isFitnessReadGranted) {
                AdditionalPermissionStrings(
                    title =
                        R.string.background_read_medical_combined_request_title_both_types_granted,
                    description =
                        R.string
                            .background_read_medical_combined_request_description_both_types_granted,
                )
            } else if (isMedicalReadGranted) {
                AdditionalPermissionStrings(
                    title = R.string.background_read_medical_combined_request_title_medical_granted,
                    description =
                        R.string
                            .background_read_medical_combined_request_description_medical_granted,
                )
            } else if (isFitnessReadGranted) {
                AdditionalPermissionStrings(
                    title = R.string.background_read_medical_combined_request_title_fitness_granted,
                    description =
                        R.string
                            .background_read_medical_combined_request_description_fitness_granted,
                )
            } else {
                throw IllegalArgumentException(
                    "No strings for this state of additional permission " +
                        "${HealthPermission.AdditionalPermission.READ_HEALTH_DATA_IN_BACKGROUND}"
                )
            }
        }
        AccessType.ACCESS -> {
            if (isMedicalReadGranted && isFitnessReadGranted) {
                AdditionalPermissionStrings(
                    title = R.string.background_read_medical_access_title_both_types_granted,
                    description =
                        R.string.background_read_medical_access_description_both_types_granted,
                )
            } else if (isMedicalReadGranted) {
                AdditionalPermissionStrings(
                    title = R.string.background_read_medical_access_title_medical_granted,
                    description =
                        R.string.background_read_medical_access_description_medical_granted,
                )
            } else if (isFitnessReadGranted) {
                AdditionalPermissionStrings(
                    title = R.string.background_read_medical_access_title_fitness_granted,
                    description =
                        R.string.background_read_medical_access_description_fitness_granted,
                )
            } else {
                // text to show when disabled
                AdditionalPermissionStrings(
                    title = R.string.background_read_medical_access_title_both_types_granted,
                    description =
                        R.string.background_read_medical_access_description_both_types_granted,
                )
            }
        }
    }
}

/** Defines the type of strings shown for additional permissions */
enum class AccessType {
    SINGLE_REQUEST,
    COMBINED_REQUEST,
    ACCESS,
}
