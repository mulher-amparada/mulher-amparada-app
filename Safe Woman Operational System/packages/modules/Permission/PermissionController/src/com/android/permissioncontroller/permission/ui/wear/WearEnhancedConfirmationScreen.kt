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

package com.android.permissioncontroller.permission.ui.wear

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.wear.compose.foundation.SwipeToDismissValue
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.SwipeToDismissBox
import com.android.permissioncontroller.R
import com.android.permissioncontroller.permission.ui.wear.model.WearEnhancedConfirmationViewModel
import com.android.permissioncontroller.permission.ui.wear.model.WearEnhancedConfirmationViewModel.ScreenState
import com.android.permissioncontroller.wear.permission.components.CheckYourPhoneScreen
import com.android.permissioncontroller.wear.permission.components.CheckYourPhoneState
import com.android.permissioncontroller.wear.permission.components.CheckYourPhoneState.InProgress
import com.android.permissioncontroller.wear.permission.components.CheckYourPhoneState.Success
import com.android.permissioncontroller.wear.permission.components.ScrollableScreen
import com.android.permissioncontroller.wear.permission.components.dismiss
import com.android.permissioncontroller.wear.permission.components.findActivity
import com.android.permissioncontroller.wear.permission.components.material2.Chip
import com.android.permissioncontroller.wear.permission.components.material3.DialogButtonContent
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionConfirmationDialog
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper

@Composable
fun WearEnhancedConfirmationScreen(
    viewModel: WearEnhancedConfirmationViewModel,
    title: String?,
    message: CharSequence?,
) {
    val materialUIVersion = ResourceHelper.materialUIVersionInSettings
    var dismissed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val ecmScreenState = remember { viewModel.screenState }
    val activity = context.findActivity()

    val state = rememberSwipeToDismissBoxState()

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissValue.Dismissed) {
            dismiss(activity)
            dismissed = true
            state.snapTo(SwipeToDismissValue.Default)
        }
    }

    fun dismiss(activity: Activity) {
        if (activity is FragmentActivity) {
            if (!activity.supportFragmentManager.popBackStackImmediate()) {
                activity.finish()
            }
        } else {
            activity.finish()
        }
    }

    @Composable
    fun ShowECMRestrictionDialog() =
        ScrollableScreen(
            showTimeText = false,
            title = title,
            subtitle = message,
            imageBuilder =
                WearPermissionIconBuilder.builder(R.drawable.ic_android_security_privacy),
            content = {
                item {
                    Chip(
                        label = stringResource(R.string.enhanced_confirmation_dialog_ok),
                        onClick = { dismiss(activity) },
                        modifier = Modifier.fillMaxWidth(),
                        textColor = MaterialTheme.colors.surface,
                        colors = ChipDefaults.primaryChipColors(),
                    )
                }
                item {
                    Chip(
                        label = stringResource(R.string.enhanced_confirmation_dialog_learn_more),
                        onClick = { viewModel.openUriOnPhone(context) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )

    @Composable
    fun ShowCheckYourPhoneDialog(state: CheckYourPhoneState) =
        CheckYourPhoneScreen(
            title = stringResource(id = R.string.wear_check_your_phone_title),
            state = state,
        )

    @Composable
    fun ShowRemoteConnectionErrorDialog() =
        WearPermissionConfirmationDialog(
            materialUIVersion = materialUIVersion,
            show = true,
            title = stringResource(R.string.wear_phone_connection_error),
            message = stringResource(R.string.wear_phone_connection_should_retry),
            iconRes = WearPermissionIconBuilder.builder(R.drawable.ic_error),
            positiveButtonContent =
                DialogButtonContent(
                    icon = WearPermissionIconBuilder.builder(R.drawable.ic_refresh),
                    onClick = { viewModel.openUriOnPhone(context) },
                ),
            negativeButtonContent = DialogButtonContent(onClick = { dismiss(activity) }),
        )

    SwipeToDismissBox(state = state) { isBackground ->
        if (isBackground || dismissed) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            when (ecmScreenState.value) {
                ScreenState.SHOW_RESTRICTION_DIALOG -> ShowECMRestrictionDialog()
                ScreenState.SHOW_CONNECTION_IN_PROGRESS -> ShowCheckYourPhoneDialog(InProgress)
                ScreenState.SHOW_CONNECTION_ERROR -> ShowRemoteConnectionErrorDialog()
                ScreenState.SHOW_CONNECTION_SUCCESS -> ShowCheckYourPhoneDialog(Success)
            }
        }
    }
}
