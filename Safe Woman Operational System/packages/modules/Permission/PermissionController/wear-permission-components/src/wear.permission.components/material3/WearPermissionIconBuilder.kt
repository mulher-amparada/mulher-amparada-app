/*
 * Copyright 2024 The Android Open Source Project
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
package com.android.permissioncontroller.wear.permission.components.material3

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonDefaults
import com.android.permissioncontroller.wear.permission.components.rememberDrawablePainter

/**
 * This class simplifies the construction of icons with various attributes like resource type,
 * content description, modifier, and tint. It supports different icon resource types, including:
 * - ImageVector
 * - Resource ID (Int)
 * - Drawable
 * - ImageBitmap
 *
 * Usage:
 * ```
 * val icon = WearPermissionIconBuilder.builder(IconResourceId)
 *      .contentDescription("Location Permission")
 *      .modifier(Modifier.size(24.dp))
 *      .tint(Color.Red)
 *      .build()
 * ```
 *
 * Note: This builder uses a private constructor and is initialized through the `builder()`
 * companion object method.
 */
class WearPermissionIconBuilder private constructor() {
    var iconResource: Any? = null
        private set

    var contentDescription: String? = null
        private set

    var modifier: Modifier = Modifier.size(IconButtonDefaults.LargeIconSize)
        private set

    var tint: Color = Color.Unspecified
        private set

    fun contentDescription(description: String?): WearPermissionIconBuilder {
        contentDescription = description
        return this
    }

    fun modifier(modifier: Modifier): WearPermissionIconBuilder {
        this.modifier = modifier then this.modifier
        return this
    }

    fun tint(tint: Color): WearPermissionIconBuilder {
        this.tint = tint
        return this
    }

    // To be used for non tintable images.
    // MultiColored assets such as Badged app icons are not tintable. They are not rendered properly
    // as Icon composable.
    @Composable
    fun buildAsImage() {
        val colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint)
        when (iconResource) {
            is ImageVector ->
                Image(
                    imageVector = iconResource as ImageVector,
                    contentDescription,
                    modifier,
                    colorFilter = colorFilter,
                )
            is Int ->
                Image(
                    painterResource(id = iconResource as Int),
                    contentDescription,
                    modifier,
                    colorFilter = colorFilter,
                )

            is Drawable ->
                Image(
                    rememberDrawablePainter(iconResource as Drawable),
                    contentDescription,
                    modifier,
                    colorFilter = colorFilter,
                )

            is ImageBitmap ->
                Image(
                    iconResource as ImageBitmap,
                    contentDescription,
                    modifier,
                    colorFilter = colorFilter,
                )
            else -> throw IllegalArgumentException("Type not supported.")
        }
    }

    // We can use Icon composable when we don't have to deal multicolored non tintable assets.
    // Icon composable is concise and comes with convenient defaults from material libraries that
    // work well with local color scheme when attributes are not explicit.
    @Composable
    fun build() {
        when (iconResource) {
            is ImageVector -> Icon(iconResource as ImageVector, contentDescription, modifier, tint)
            is Int ->
                Icon(painterResource(id = iconResource as Int), contentDescription, modifier, tint)

            is Drawable ->
                Icon(
                    rememberDrawablePainter(iconResource as Drawable),
                    contentDescription,
                    modifier,
                    tint,
                )

            is ImageBitmap -> Icon(iconResource as ImageBitmap, contentDescription, modifier, tint)
            else -> throw IllegalArgumentException("Type not supported.")
        }
    }

    companion object {
        fun builder(icon: Any) = WearPermissionIconBuilder().apply { iconResource = icon }
    }
}

@Composable
fun WearPermissionIconBuilder.Companion.defaultAlertConfirmIcon() =
    builder(Icons.Default.Check).contentDescription((stringResource(android.R.string.ok)))

@Composable
fun WearPermissionIconBuilder.Companion.defaultAlertDismissIcon() =
    builder(Icons.Default.Close).contentDescription((stringResource(android.R.string.cancel)))
