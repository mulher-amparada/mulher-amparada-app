/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.android.permissioncontroller.safetycenter.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager

/** A [PreferenceComparisonCallback] to identify changed preferences of Safety Center. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class SafetyPreferenceComparisonCallback :
    PreferenceManager.SimplePreferenceComparisonCallback() {
    override fun arePreferenceItemsTheSame(
        oldPreference: Preference,
        newPreference: Preference,
    ): Boolean {
        if (oldPreference is ComparablePreference) {
            return oldPreference.isSameItem(newPreference)
        }
        if (oldPreference is PreferenceCategory && newPreference is PreferenceCategory) {
            return oldPreference::class == newPreference::class &&
                oldPreference.key == newPreference.key
        }
        return super.arePreferenceItemsTheSame(oldPreference, newPreference)
    }

    override fun arePreferenceContentsTheSame(
        oldPreference: Preference,
        newPreference: Preference,
    ): Boolean {
        if (oldPreference is ComparablePreference) {
            return oldPreference.hasSameContents(newPreference)
        }
        if (oldPreference is PreferenceCategory && newPreference is PreferenceCategory) {
            return oldPreference.title == newPreference.title
        }
        return super.arePreferenceContentsTheSame(oldPreference, newPreference)
    }
}
