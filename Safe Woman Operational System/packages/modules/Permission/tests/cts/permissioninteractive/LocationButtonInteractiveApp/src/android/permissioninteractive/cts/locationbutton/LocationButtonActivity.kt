/*
 * Copyright (C) 2026 The Android Open Source Project
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

package android.permissioninteractive.cts.locationbutton

import android.app.Activity
import android.app.permissionui.LocationButtonRequest
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout

class LocationButtonActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val width = intent.getIntExtra(EXTRA_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT)
        val height = intent.getIntExtra(EXTRA_HEIGHT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val locationButton = LocationButton(this)
        val buttonParams =
            LinearLayout.LayoutParams(width, height).apply {
                bottomMargin = (resources.displayMetrics.density * 16).toInt()
            }
        locationButton.layoutParams = buttonParams

        val config =
            android.content.res.Configuration(resources.configuration).apply {
                intent.getStringExtra(EXTRA_LANGUAGE_TAG)?.let { languageTag ->
                    setLocales(android.os.LocaleList.forLanguageTags(languageTag))
                }
            }
        val requestBuilder = LocationButtonRequest.Builder(width, height, config)

        val extras = intent.extras ?: Bundle.EMPTY
        for (key in extras.keySet()) {
            when (key) {
                EXTRA_BACKGROUND_COLOR -> requestBuilder.setBackgroundColor(extras.getInt(key))
                EXTRA_TEXT_COLOR -> requestBuilder.setTextColor(extras.getInt(key))
                EXTRA_ICON_TINT -> requestBuilder.setIconTint(extras.getInt(key))
                EXTRA_STROKE_COLOR -> requestBuilder.setStrokeColor(extras.getInt(key))
                EXTRA_STROKE_WIDTH -> requestBuilder.setStrokeWidth(extras.getInt(key))
                EXTRA_CORNER_RADIUS -> requestBuilder.setCornerRadius(extras.getFloat(key))
                EXTRA_PRESSED_CORNER_RADIUS ->
                    requestBuilder.setPressedCornerRadius(extras.getFloat(key))
                EXTRA_TEXT_TYPE -> requestBuilder.setTextType(extras.getInt(key))
                EXTRA_PADDING_LEFT -> requestBuilder.setPaddingLeft(extras.getInt(key))
                EXTRA_PADDING_TOP -> requestBuilder.setPaddingTop(extras.getInt(key))
                EXTRA_PADDING_RIGHT -> requestBuilder.setPaddingRight(extras.getInt(key))
                EXTRA_PADDING_BOTTOM -> requestBuilder.setPaddingBottom(extras.getInt(key))
            }
        }

        locationButton.setRequest(requestBuilder.build())

        val container =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                fitsSystemWindows = true
            }

        if (intent.getBooleanExtra(EXTRA_SHOW_REFERENCE_BUTTON, false)) {
            val referenceButton = LocationButton(this)
            requestBuilder.setPaddingLeft(0).setPaddingTop(0).setPaddingRight(0).setPaddingBottom(0)
            referenceButton.setRequest(requestBuilder.build())
            referenceButton.layoutParams = LinearLayout.LayoutParams(buttonParams)
            container.addView(referenceButton)
        }

        container.addView(locationButton)
        setContentView(container)
    }

    companion object {
        const val EXTRA_WIDTH = "width"
        const val EXTRA_HEIGHT = "height"
        const val EXTRA_BACKGROUND_COLOR = "background_color"
        const val EXTRA_TEXT_COLOR = "text_color"
        const val EXTRA_ICON_TINT = "icon_tint"
        const val EXTRA_STROKE_COLOR = "stroke_color"
        const val EXTRA_STROKE_WIDTH = "stroke_width"
        const val EXTRA_CORNER_RADIUS = "corner_radius"
        const val EXTRA_PRESSED_CORNER_RADIUS = "pressed_corner_radius"
        const val EXTRA_TEXT_TYPE = "text_type"
        const val EXTRA_LANGUAGE_TAG = "language_tag"
        const val EXTRA_PADDING_LEFT = "padding_left"
        const val EXTRA_PADDING_TOP = "padding_top"
        const val EXTRA_PADDING_RIGHT = "padding_right"
        const val EXTRA_PADDING_BOTTOM = "padding_bottom"
        const val EXTRA_SHOW_REFERENCE_BUTTON = "show_reference_button"
        const val LOCATION_BUTTON_CONTENT_DESCRIPTION = "location_button"
    }
}
