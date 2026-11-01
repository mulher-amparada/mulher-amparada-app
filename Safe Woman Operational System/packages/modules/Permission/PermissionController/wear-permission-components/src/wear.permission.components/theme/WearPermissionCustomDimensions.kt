/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.permissioncontroller.wear.permission.components.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.permissioncontroller.wear.permission.components.R

data class WearPermissionCustomDimensions(
    val scrollScreenTitleIconSize: Int,
    val buttonMinHeight: Int,
    val toggleMinHeight: Int,
    val scrollHorizontalPaddingMultiplier: Float,
    val scrollTopMultiplier: Float,
    val scrollBottomMultiplier: Float,
    val titleHorizontalPaddingMultiplier: Float,
    val subtitleHorizontalPaddingMultiplier: Float,
) {
    companion object {
        val default =
            WearPermissionCustomDimensions(
                scrollScreenTitleIconSize = 32,
                buttonMinHeight = 52,
                toggleMinHeight = 52,
                scrollHorizontalPaddingMultiplier = 0.052f,
                scrollTopMultiplier = 0.1664f,
                scrollBottomMultiplier = 0.3646f,
                titleHorizontalPaddingMultiplier = 0.1200f,
                subtitleHorizontalPaddingMultiplier = 0.0416f,
            )
    }
}

@Composable
fun rememberWearPermissionCustomDimensions(): WearPermissionCustomDimensions {
    val context = LocalContext.current.applicationContext

    return remember(context) {
        WearPermissionCustomDimensions(
            scrollHorizontalPaddingMultiplier =
                ResourceHelper.getDimen(
                    context,
                    R.dimen.scroll_content_horizontal_padding_multiplier,
                ) ?: WearPermissionCustomDimensions.default.subtitleHorizontalPaddingMultiplier,
            scrollTopMultiplier =
                ResourceHelper.getDimen(context, R.dimen.scroll_content_top_padding_multiplier)
                    ?: WearPermissionCustomDimensions.default.scrollTopMultiplier,
            scrollBottomMultiplier =
                ResourceHelper.getDimen(context, R.dimen.scroll_content_bottom_padding_multiplier)
                    ?: WearPermissionCustomDimensions.default.scrollBottomMultiplier,
            titleHorizontalPaddingMultiplier =
                ResourceHelper.getDimen(
                    context,
                    R.dimen.scroll_title_additional_horizonal_padding_multiplier,
                ) ?: WearPermissionCustomDimensions.default.titleHorizontalPaddingMultiplier,
            subtitleHorizontalPaddingMultiplier =
                ResourceHelper.getDimen(
                    context,
                    R.dimen.scroll_subtitle_additional_horizonal_padding_multiplier,
                ) ?: WearPermissionCustomDimensions.default.subtitleHorizontalPaddingMultiplier,
            scrollScreenTitleIconSize =
                ResourceHelper.getDimen(context, R.dimen.title_icon_size)?.toInt()
                    ?: WearPermissionCustomDimensions.default.scrollScreenTitleIconSize,
            buttonMinHeight =
                ResourceHelper.getDimen(context, R.dimen.button_min_height)?.toInt()
                    ?: WearPermissionCustomDimensions.default.buttonMinHeight,
            toggleMinHeight =
                ResourceHelper.getDimen(context, R.dimen.toggle_min_height)?.toInt()
                    ?: WearPermissionCustomDimensions.default.toggleMinHeight,
        )
    }
}
