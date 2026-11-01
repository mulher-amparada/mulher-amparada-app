/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.permissioncontroller.wear.permission.components.material2

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.LocalTextStyle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import com.android.permissioncontroller.wear.permission.components.material2.layout.ScalingLazyColumnDefaults
import com.android.permissioncontroller.wear.permission.components.material2.layout.ScalingLazyColumnState
import com.android.permissioncontroller.wear.permission.components.material2.layout.rememberColumnState
import com.android.permissioncontroller.wear.permission.components.material3.DialogButtonContent
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder

/**
 * This component is an alternative to [AlertContent], providing the following:
 * - a convenient way of passing a title and a message;
 * - additional content can be specified between the message and the buttons
 * - default positive and negative buttons;
 * - wrapped in a [Dialog];
 */
@Composable
fun AlertDialog(
    title: String? = null,
    message: String,
    positiveButtonContent: DialogButtonContent?,
    negativeButtonContent: DialogButtonContent?,
    showDialog: Boolean,
    modifier: Modifier = Modifier,
    iconRes: WearPermissionIconBuilder? = null,
    scalingLazyListState: ScalingLazyListState,
) {
    Dialog(
        showDialog = showDialog,
        onDismissRequest = { negativeButtonContent?.onClick?.invoke() },
        scrollState = scalingLazyListState,
        modifier = modifier,
    ) {
        AlertContent(
            title = title,
            icon = { iconRes?.build() },
            message = message,
            positiveButtonContent = positiveButtonContent,
            negativeButtonContent = negativeButtonContent,
        )
    }
}

@Composable
fun AlertContent(
    icon: @Composable (() -> Unit)? = null,
    title: String? = null,
    message: String? = null,
    positiveButtonContent: DialogButtonContent?,
    negativeButtonContent: DialogButtonContent?,
    state: ScalingLazyColumnState =
        rememberColumnState(ScalingLazyColumnDefaults.responsive(additionalPaddingAtBottom = 0.dp)),
    showPositionIndicator: Boolean = true,
    content: (ScalingLazyListScope.() -> Unit)? = null,
) {
    val density = LocalDensity.current
    val maxScreenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    ResponsiveDialogContent(
        icon = icon,
        title =
            title?.let {
                {
                    Text(
                        modifier = Modifier.fillMaxWidth().semantics() { heading() },
                        text = it,
                        color = MaterialTheme.colors.onBackground,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
        message =
            message?.let {
                {
                    // Should message be start or center aligned?
                    val textMeasurer = rememberTextMeasurer()
                    val textStyle = LocalTextStyle.current
                    val totalPaddingPercentage =
                        globalHorizontalPadding + messageExtraHorizontalPadding
                    val lineCount =
                        remember(it, density, textStyle, textMeasurer) {
                            textMeasurer
                                .measure(
                                    text = it,
                                    style = textStyle,
                                    constraints =
                                        Constraints(
                                            // Available width is reduced by responsive dialog
                                            // horizontal
                                            // padding.
                                            maxWidth =
                                                (maxScreenWidthPx *
                                                        (1f - totalPaddingPercentage * 2f / 100f))
                                                    .toInt()
                                        ),
                                )
                                .lineCount
                        }
                    val textAlign = if (lineCount <= 3) TextAlign.Center else TextAlign.Start
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = it,
                        color = MaterialTheme.colors.onBackground,
                        textAlign = textAlign,
                    )
                }
            },
        content = content,
        positiveButtonContent = positiveButtonContent,
        negativeButtonContent = negativeButtonContent,
        state = state,
        showPositionIndicator = showPositionIndicator,
    )
}
