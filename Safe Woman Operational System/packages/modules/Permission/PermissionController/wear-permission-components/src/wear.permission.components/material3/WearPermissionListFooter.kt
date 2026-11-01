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
package com.android.permissioncontroller.wear.permission.components.material3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.LocalContentColor
import androidx.wear.compose.material3.LocalTextConfiguration
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.TextConfiguration
import com.android.permissioncontroller.wear.permission.components.material2.ListFooter
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionMaterialUIVersion

@Composable
fun WearPermissionListFooter(
    label: WearPermissionTextProvider,
    modifier: Modifier = Modifier,
    materialUIVersion: WearPermissionMaterialUIVersion = ResourceHelper.materialUIVersionInSettings,
    iconBuilder: WearPermissionIconBuilder? = null,
    onClick: (() -> Unit)? = null,
) {
    if (materialUIVersion == WearPermissionMaterialUIVersion.MATERIAL2_5) {
        ListFooter(
            description = label.toString(),
            iconRes = iconBuilder?.iconResource as? Int,
            onClick = onClick,
        )
    } else {
        ListFooterTextProvider {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .requiredHeightIn(min = 1.dp)
                        .then(
                            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
                        )
            ) {
                iconBuilder?.let {
                    it.build()
                    Spacer(Modifier.height(4.dp))
                }
                WearPermissionText(label)
            }
        }
    }
}

@Composable
internal fun ListFooterTextProvider(content: @Composable () -> Unit) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val typography = MaterialTheme.typography.labelSmall
    val textStyle = remember(typography) { typography.copy(hyphens = Hyphens.Auto) }
    val textConfiguration = remember {
        TextConfiguration(
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 30,
        )
    }

    CompositionLocalProvider(
        LocalContentColor provides color,
        LocalTextStyle provides textStyle,
        LocalTextConfiguration provides textConfiguration,
    ) {
        content()
    }
}
