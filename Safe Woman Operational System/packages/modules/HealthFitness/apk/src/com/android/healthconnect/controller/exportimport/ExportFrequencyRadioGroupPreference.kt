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

package com.android.healthconnect.controller.exportimport

import android.content.Context
import android.widget.RadioGroup
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.exportimport.api.ExportFrequency
import com.android.healthconnect.controller.utils.logging.HealthConnectLogger
import com.android.healthconnect.controller.utils.logging.HealthConnectLoggerEntryPoint
import com.android.healthconnect.controller.utils.logging.ScheduledExportElement
import dagger.hilt.android.EntryPointAccessors

/** Preference for updating export frequency. */
class ExportFrequencyRadioGroupPreference(
    context: Context,
    private var exportFrequency: ExportFrequency,
    private val updateExportFrequency: (frequency: ExportFrequency) -> Unit
) : Preference(context) {

    private var logger: HealthConnectLogger

    companion object {
        const val EXPORT_FREQUENCY_RADIO_GROUP_PREFERENCE =
            "export_frequency_radio_group_preference"
    }

    init {
        layoutResource = R.layout.widget_export_frequency_radio_group_preference
        key = EXPORT_FREQUENCY_RADIO_GROUP_PREFERENCE

        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext, HealthConnectLoggerEntryPoint::class.java)
        logger = hiltEntryPoint.logger()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val radioGroup = holder.findViewById(R.id.radio_group_frequency) as RadioGroup

        when (exportFrequency) {
            ExportFrequency.EXPORT_FREQUENCY_WEEKLY -> radioGroup.check(R.id.radio_button_weekly)
            ExportFrequency.EXPORT_FREQUENCY_MONTHLY -> radioGroup.check(R.id.radio_button_monthly)
            ExportFrequency.EXPORT_FREQUENCY_DAILY -> radioGroup.check(R.id.radio_button_daily)
            // This shouldn't happen as the preference will be invisible if the export frequency is
            // NEVER.
            ExportFrequency.EXPORT_FREQUENCY_NEVER -> radioGroup.check(R.id.radio_button_daily)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_button_daily -> {
                    exportFrequency = ExportFrequency.EXPORT_FREQUENCY_DAILY
                    updateExportFrequency(ExportFrequency.EXPORT_FREQUENCY_DAILY)
                    logger.logInteraction(ScheduledExportElement.EXPORT_SETTINGS_FREQUENCY_DAILY)
                }
                R.id.radio_button_weekly -> {
                    exportFrequency = ExportFrequency.EXPORT_FREQUENCY_WEEKLY
                    updateExportFrequency(ExportFrequency.EXPORT_FREQUENCY_WEEKLY)
                    logger.logInteraction(ScheduledExportElement.EXPORT_SETTINGS_FREQUENCY_WEEKLY)
                }
                R.id.radio_button_monthly -> {
                    exportFrequency = ExportFrequency.EXPORT_FREQUENCY_MONTHLY
                    updateExportFrequency(ExportFrequency.EXPORT_FREQUENCY_MONTHLY)
                    logger.logInteraction(ScheduledExportElement.EXPORT_SETTINGS_FREQUENCY_MONTHLY)
                }
            }
        }
    }

    override fun onAttached() {
        super.onAttached()
        logger.logImpression(ScheduledExportElement.EXPORT_SETTINGS_FREQUENCY_DAILY)
        logger.logImpression(ScheduledExportElement.EXPORT_SETTINGS_FREQUENCY_WEEKLY)
        logger.logImpression(ScheduledExportElement.EXPORT_SETTINGS_FREQUENCY_MONTHLY)
    }
}
