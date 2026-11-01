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

package com.android.permissioncontroller.incident.wear

import android.app.Activity
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.wear.compose.material.CircularProgressIndicator
import com.android.permissioncontroller.wear.permission.components.material3.DialogButtonContent
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionConfirmationDialog
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionTheme

@Composable
fun WearConfirmationScreen(viewModel: WearConfirmationActivityViewModel) {
    val materialUIVersion = ResourceHelper.materialUIVersionInSettings
    // Wear screen doesn't show incident/bug report's optional reasons and images.
    val showDialog = viewModel.showDialogLiveData.observeAsState(false)
    val showDenyReport = viewModel.showDenyReportLiveData.observeAsState(false)
    val contentArgs = viewModel.contentArgsLiveData.observeAsState(null)
    var isLoading by rememberSaveable { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            contentArgs.value?.apply {
                if (showDenyReport.value) {
                    WearPermissionConfirmationDialog(
                        materialUIVersion = materialUIVersion,
                        show = showDialog.value,
                        title = title,
                        message = message,
                        positiveButtonContent = DialogButtonContent(onClick = onDenyClick),
                    )
                } else {
                    WearPermissionConfirmationDialog(
                        materialUIVersion = materialUIVersion,
                        show = showDialog.value,
                        title = title,
                        message = message,
                        positiveButtonContent = DialogButtonContent(onClick = onOkClick),
                        negativeButtonContent = DialogButtonContent(onClick = onCancelClick),
                    )
                }
            }
        }
    }

    if (isLoading && showDialog.value) {
        isLoading = false
    }
}

fun createView(activity: Activity, viewModel: WearConfirmationActivityViewModel): View {
    return ComposeView(activity).apply {
        setContent { WearPermissionTheme { WearConfirmationScreen(viewModel) } }
    }
}
