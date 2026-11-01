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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material3.MaterialTheme as Material3Theme
import kotlin.run

/** This enum is used to specify the material version used for a specific screen */
enum class WearPermissionMaterialUIVersion {
    MATERIAL2_5,
    MATERIAL3,
}

val LocalCustomDimensions = staticCompositionLocalOf { WearPermissionCustomDimensions.default }

/** An overlay-able compose theme supporting both material2.5 and 3. */
@Composable
fun WearPermissionTheme(
    version: WearPermissionMaterialUIVersion = ResourceHelper.materialUIVersionInSettings,
    content: @Composable () -> Unit,
) {
    val customDimensions = rememberWearPermissionCustomDimensions()
    val wearOverlayableMaterial3Theme = rememberWearOverlayableMaterial3Theme()
    CompositionLocalProvider(LocalCustomDimensions provides customDimensions) {
        wearOverlayableMaterial3Theme.run {
            when (version) {
                WearPermissionMaterialUIVersion.MATERIAL3 ->
                    Material3Theme(
                        colorScheme = colorScheme,
                        typography = typography,
                        shapes = shapes,
                        content = content,
                    )
                // Material2_5 UI controls are still being used in the screen,
                // To avoid having two set of overlay resources, we will use material3 overlay
                // resources to
                // support material2_5 UI controls as well.
                WearPermissionMaterialUIVersion.MATERIAL2_5 ->
                    rememberBridgedLegacyTheme(this).run {
                        MaterialTheme(
                            colors = colors,
                            typography = typography,
                            shapes = shapes,
                            content = content,
                        )
                    }
            }
        }
    }
}
