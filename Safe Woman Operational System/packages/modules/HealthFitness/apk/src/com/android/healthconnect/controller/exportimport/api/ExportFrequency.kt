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

package com.android.healthconnect.controller.exportimport.api

/**
 * Specifies the frequency of scheduled exports of Health Connect data (no data gets exported in
 * case of [ExportFrequency.EXPORT_FREQUENCY_NEVER]).
 *
 * @param periodInDays: period in days between scheduled exports.
 */
enum class ExportFrequency(val periodInDays: Int) {
    EXPORT_FREQUENCY_NEVER(0),
    EXPORT_FREQUENCY_DAILY(1),
    EXPORT_FREQUENCY_WEEKLY(7),
    EXPORT_FREQUENCY_MONTHLY(30)
}

/**
 * Returns [ExportFrequency] corresponding to the given period in days between scheduled exports of
 * Health Connect data (or throws in case of unsupported period in days).
 */
fun fromPeriodInDays(periodInDays: Int): ExportFrequency {
    ExportFrequency.values().forEach { frequency ->
        if (frequency.periodInDays == periodInDays) {
            return frequency
        }
    }
    throw UnsupportedOperationException(
        "Export frequency period in days $periodInDays is not supported")
}
