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
import android.util.AttributeSet
import android.view.View
import androidx.preference.Preference
import com.android.healthconnect.controller.permissions.connectedapps.ComparablePreference
import com.android.healthconnect.controller.utils.logging.ElementName
import com.android.healthconnect.controller.utils.logging.HealthConnectLogger
import com.android.healthconnect.controller.utils.logging.UnknownGenericElement
import com.android.settingslib.widget.ButtonPreference

/** A [ButtonPreference] that allows logging and has expressive theming. */
internal open class HealthButtonPreference
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null) :
    ButtonPreference(context, attrs), ComparablePreference {

    private var logger: HealthConnectLogger = HealthPreferenceUtils.initializeLogger(context)
    var logName: ElementName = UnknownGenericElement.UNKNOWN_HEALTH_PREFERENCE

    override fun onAttached() {
        super.onAttached()
        logger.logImpression(logName)
        this.setButtonStyle(/* type=tonal */ 1, /* size=normal */ 0)
    }

    override fun setOnClickListener(listener: View.OnClickListener?) {
        super.setOnClickListener(
            HealthPreferenceUtils.loggingButtonClickListener(logger, logName, listener)
        )
    }

    override fun isSameItem(preference: Preference): Boolean =
        HealthPreferenceUtils.isSameItem(preference, this)

    override fun hasSameContents(preference: Preference): Boolean =
        HealthPreferenceUtils.hasSameContents(preference, this)
}
