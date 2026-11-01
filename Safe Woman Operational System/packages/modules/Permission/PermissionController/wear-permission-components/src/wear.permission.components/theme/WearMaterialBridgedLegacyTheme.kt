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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Typography

internal data class WearMaterialBridgedLegacyTheme(
    val colors: Colors,
    val typography: Typography,
    val shapes: Shapes,
)

/**
 * This exists to support Permission Controller screens that may still use Material 2.5 components
 * to maintain consistency with the settings screens.
 *
 * However to avoid maintaining two sets of resources for overlays, this class construct 2.5 theme
 * from 3.0
 */
@Composable
internal fun rememberBridgedLegacyTheme(
    m3Theme: WearOverlayableMaterial3Theme
): WearMaterialBridgedLegacyTheme {
    // This ensures the entire mapping logic runs only when the underlying
    // M3 theme changes, not on every recomposition.
    return remember(m3Theme) {
        val colors =
            m3Theme.colorScheme.run {
                Colors(
                    background = background,
                    onBackground = onBackground,
                    primary = onPrimaryContainer, // primary90
                    primaryVariant = primaryDim, // primary80
                    onPrimary = onPrimary, // primary10
                    secondary = tertiary, // Tertiary90
                    secondaryVariant = tertiaryDim, // Tertiary60 - Tertiary80 BestFit.
                    onSecondary = onTertiary, // Tertiary10
                    surface = surfaceContainer, // neutral20
                    onSurface = onSurface, // neutral95
                    onSurfaceVariant = onSurfaceVariant, // neutralVariant80
                )
            }

        val typography =
            m3Theme.typography.run {
                Typography(
                    display1 = displayLarge, // 40.sp
                    display2 = displayMedium.copy(fontSize = 34.sp, lineHeight = 40.sp),
                    display3 = displayMedium, // 30.sp
                    title1 = displaySmall, // 24.sp
                    title2 = titleLarge, // 20.sp
                    title3 = titleMedium, // 16.sp
                    body1 = bodyLarge, // 16.sp
                    body2 = bodyMedium, // 14.sp
                    caption1 = bodyMedium, // 14.sp
                    caption2 = bodySmall, // 12.sp
                    caption3 = bodyExtraSmall, // 10.sp
                    button = labelMedium, // 15.sp
                )
            }

        val shapes = m3Theme.shapes.run { Shapes(large = large, medium = medium, small = small) }

        WearMaterialBridgedLegacyTheme(colors, typography, shapes)
    }
}
