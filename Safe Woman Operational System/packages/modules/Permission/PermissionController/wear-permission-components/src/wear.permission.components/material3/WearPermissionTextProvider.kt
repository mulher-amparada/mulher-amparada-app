/*
 * Copyright 2026 The Android Open Source Project
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.wear.compose.material3.LocalContentColor
import androidx.wear.compose.material3.Text

sealed class WearPermissionTextProvider {
    data class Plain(val text: String) : WearPermissionTextProvider()

    data class AnnotatedHtml(val text: AnnotatedString) : WearPermissionTextProvider()

    override fun toString(): String =
        when (this) {
            is Plain -> text
            is AnnotatedHtml -> text.toString()
        }
}

@Composable
fun WearPermissionText(
    textProvider: WearPermissionTextProvider,
    color: Color = LocalContentColor.current,
) {
    when (textProvider) {
        is WearPermissionTextProvider.Plain -> Text(text = textProvider.text, color = color)
        is WearPermissionTextProvider.AnnotatedHtml -> Text(text = textProvider.text, color = color)
    }
}
