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
package android.permissionui.cts.locationbutton

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout

class LocationButtonActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val width = intent.getIntExtra(EXTRA_WIDTH, ViewGroup.LayoutParams.WRAP_CONTENT)
        val height = intent.getIntExtra(EXTRA_HEIGHT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val locationButton = LocationButton(this)
        if (intent.hasExtra(EXTRA_TEXT_TYPE)) {
            locationButton.textType = intent.getIntExtra(EXTRA_TEXT_TYPE, 0)
        }
        val buttonParams =
            FrameLayout.LayoutParams(width, height).apply { gravity = android.view.Gravity.CENTER }

        val container =
            FrameLayout(this).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                setBackgroundColor(0xFFFFFFFF.toInt())
            }
        container.addView(locationButton, buttonParams)
        setContentView(container)
    }

    companion object {
        const val EXTRA_WIDTH = "width"
        const val EXTRA_HEIGHT = "height"
        const val EXTRA_TEXT_TYPE = "text_type"
    }
}
