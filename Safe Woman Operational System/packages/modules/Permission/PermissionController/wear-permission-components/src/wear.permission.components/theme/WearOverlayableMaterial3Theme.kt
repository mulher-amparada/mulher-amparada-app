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
package com.android.permissioncontroller.wear.permission.components.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.Shapes
import androidx.wear.compose.material3.Typography

internal data class WearOverlayableMaterial3Theme(
    val colorScheme: ColorScheme,
    val typography: Typography,
    val shapes: Shapes,
)

/**
 * Theme wrapper providing Material 3 styling while maintaining compatibility with Runtime Resource
 * Overlay (RRO).
 *
 * Uses the tonal palette from the previous Material Design version until dynamic color tokens are
 * available in SDK 36.
 */
@Composable
internal fun rememberWearOverlayableMaterial3Theme(): WearOverlayableMaterial3Theme {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        WearOverlayableMaterial3Theme(
            colorScheme =
                when {
                    Build.VERSION.SDK_INT >= 36 -> {
                        WearComposeMaterial3ColorScheme.dynamicColorScheme(context)
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        WearComposeMaterial3ColorScheme.tonalColorScheme(context)
                    }

                    else -> {
                        WearComposeMaterial3ColorScheme.legacyColorScheme()
                    }
                },
            typography = WearComposeMaterial3Typography.dynamicTypography(context),
            shapes = WearComposeMaterial3Shapes.dynamicShapes(context),
        )
    }
}
