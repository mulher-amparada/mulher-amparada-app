package com.android.healthconnect.controller.shared.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.healthconnect.controller.R

/** Custom preference for displaying no search result. */
class NoDataPreference
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle,
) : Preference(context, attrs, defStyleAttr) {

    init {
        key = "no_data_preference"
        setSummary(R.string.no_data)
        isSelectable = false
    }
}
