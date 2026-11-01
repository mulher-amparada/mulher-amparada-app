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
package com.android.healthconnect.controller.shared.preference

import android.content.Context
import android.view.View
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import com.android.healthconnect.controller.utils.logging.ElementName
import com.android.healthconnect.controller.utils.logging.HealthConnectLogger
import com.android.healthconnect.controller.utils.logging.HealthConnectLoggerEntryPoint
import dagger.hilt.android.EntryPointAccessors

internal object HealthPreferenceUtils {
    fun initializeLogger(context: Context): HealthConnectLogger {
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                HealthConnectLoggerEntryPoint::class.java,
            )
        return hiltEntryPoint.logger()
    }

    fun loggingPreferenceClickListener(
        logger: HealthConnectLogger,
        logName: ElementName,
        onPreferenceClickListener: OnPreferenceClickListener?,
    ): OnPreferenceClickListener = OnPreferenceClickListener {
        logger.logInteraction(logName)
        onPreferenceClickListener?.onPreferenceClick(it) ?: false
    }

    fun loggingButtonClickListener(
        logger: HealthConnectLogger,
        logName: ElementName,
        onClickListener: View.OnClickListener?,
    ): View.OnClickListener =
        View.OnClickListener {
            logger.logInteraction(logName)
            onClickListener?.onClick(it)
        }

    fun isSameItem(preference1: Preference, preference2: Preference): Boolean {
        return preference1 === preference2
    }

    fun hasSameContents(preference1: Preference, preference2: Preference): Boolean {
        return preference1::class == preference2::class &&
            preference1.title == preference2.title &&
            preference1.summary == preference2.summary &&
            preference1.icon == preference2.icon &&
            preference1.isEnabled == preference2.isEnabled
    }
}
