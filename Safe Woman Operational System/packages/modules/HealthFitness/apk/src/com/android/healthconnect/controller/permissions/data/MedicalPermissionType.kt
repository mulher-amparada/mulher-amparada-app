/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * ```
 *      http://www.apache.org/licenses/LICENSE-2.0
 * ```
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.controller.permissions.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_ALLERGIES_INTOLERANCES
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_CONDITIONS
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_VACCINES
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_LABORATORY_RESULTS
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_MEDICATIONS
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_PERSONAL_DETAILS
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_PRACTITIONER_DETAILS
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_PREGNANCY
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_PROCEDURES
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_SOCIAL_HISTORY
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_VISITS
import android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_VITAL_SIGNS
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.utils.AttributeResolver

enum class MedicalPermissionType : HealthPermissionType {
    ALL_MEDICAL_DATA,
    ALLERGIES_INTOLERANCES,
    CONDITIONS,
    LABORATORY_RESULTS,
    MEDICATIONS,
    PERSONAL_DETAILS,
    PRACTITIONER_DETAILS,
    PREGNANCY,
    PROCEDURES,
    SOCIAL_HISTORY,
    VACCINES,
    VISITS,
    VITAL_SIGNS;

    override fun lowerCaseLabel(): Int =
        MedicalPermissionStrings.fromPermissionType(this).lowercaseLabel

    override fun upperCaseLabel(): Int =
        MedicalPermissionStrings.fromPermissionType(this).uppercaseLabel

    override fun icon(context: Context): Drawable? {
        val attrRes: Int =
            when (this) {
                ALL_MEDICAL_DATA -> R.attr.medicalServicesIcon
                ALLERGIES_INTOLERANCES -> R.attr.allergiesIcon
                CONDITIONS -> R.attr.conditionsIcon
                LABORATORY_RESULTS -> R.attr.labResultsIcon
                MEDICATIONS -> R.attr.medicationsIcon
                PERSONAL_DETAILS -> R.attr.patientInfoIcon
                PRACTITIONER_DETAILS -> R.attr.practitionerDetailsIcon
                PREGNANCY -> R.attr.pregnancyIcon
                PROCEDURES -> R.attr.proceduresIcon
                SOCIAL_HISTORY -> R.attr.socialHistoryIcon
                VACCINES -> R.attr.immunizationIcon
                VISITS -> R.attr.pastVisitsIcon
                VITAL_SIGNS -> R.attr.vitalsIcon
            }
        return AttributeResolver.getDrawable(context, attrRes)
    }
}

fun isValidMedicalPermissionType(permissionTypeString: String): Boolean {
    try {
        MedicalPermissionType.valueOf(permissionTypeString)
    } catch (e: IllegalArgumentException) {
        return false
    }
    return true
}

fun fromMedicalResourceType(medicalResourceType: Int): MedicalPermissionType {
    return when (medicalResourceType) {
        MEDICAL_RESOURCE_TYPE_ALLERGIES_INTOLERANCES -> MedicalPermissionType.ALLERGIES_INTOLERANCES
        MEDICAL_RESOURCE_TYPE_CONDITIONS -> MedicalPermissionType.CONDITIONS
        MEDICAL_RESOURCE_TYPE_LABORATORY_RESULTS -> MedicalPermissionType.LABORATORY_RESULTS
        MEDICAL_RESOURCE_TYPE_MEDICATIONS -> MedicalPermissionType.MEDICATIONS
        MEDICAL_RESOURCE_TYPE_PERSONAL_DETAILS -> MedicalPermissionType.PERSONAL_DETAILS
        MEDICAL_RESOURCE_TYPE_PRACTITIONER_DETAILS -> MedicalPermissionType.PRACTITIONER_DETAILS
        MEDICAL_RESOURCE_TYPE_PREGNANCY -> MedicalPermissionType.PREGNANCY
        MEDICAL_RESOURCE_TYPE_PROCEDURES -> MedicalPermissionType.PROCEDURES
        MEDICAL_RESOURCE_TYPE_SOCIAL_HISTORY -> MedicalPermissionType.SOCIAL_HISTORY
        MEDICAL_RESOURCE_TYPE_VACCINES -> MedicalPermissionType.VACCINES
        MEDICAL_RESOURCE_TYPE_VISITS -> MedicalPermissionType.VISITS
        MEDICAL_RESOURCE_TYPE_VITAL_SIGNS -> MedicalPermissionType.VITAL_SIGNS
        else -> throw IllegalArgumentException("MedicalResourceType is not supported.")
    }
}

fun toMedicalResourceType(medicalPermissionType: MedicalPermissionType): Int {
    return when (medicalPermissionType) {
        MedicalPermissionType.ALLERGIES_INTOLERANCES -> MEDICAL_RESOURCE_TYPE_ALLERGIES_INTOLERANCES
        MedicalPermissionType.CONDITIONS -> MEDICAL_RESOURCE_TYPE_CONDITIONS
        MedicalPermissionType.LABORATORY_RESULTS -> MEDICAL_RESOURCE_TYPE_LABORATORY_RESULTS
        MedicalPermissionType.MEDICATIONS -> MEDICAL_RESOURCE_TYPE_MEDICATIONS
        MedicalPermissionType.PERSONAL_DETAILS -> MEDICAL_RESOURCE_TYPE_PERSONAL_DETAILS
        MedicalPermissionType.PRACTITIONER_DETAILS -> MEDICAL_RESOURCE_TYPE_PRACTITIONER_DETAILS
        MedicalPermissionType.PREGNANCY -> MEDICAL_RESOURCE_TYPE_PREGNANCY
        MedicalPermissionType.PROCEDURES -> MEDICAL_RESOURCE_TYPE_PROCEDURES
        MedicalPermissionType.SOCIAL_HISTORY -> MEDICAL_RESOURCE_TYPE_SOCIAL_HISTORY
        MedicalPermissionType.VACCINES -> MEDICAL_RESOURCE_TYPE_VACCINES
        MedicalPermissionType.VISITS -> MEDICAL_RESOURCE_TYPE_VISITS
        MedicalPermissionType.VITAL_SIGNS -> MEDICAL_RESOURCE_TYPE_VITAL_SIGNS
        else -> throw IllegalArgumentException("MedicalPermissionType does not map to a MedicalResourceType.")
    }
}
