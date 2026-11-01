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

package com.android.permissioncontroller.permission.ui.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.permissioncontroller.R
import com.android.permissioncontroller.permission.ui.model.AppPermissionViewModel
import com.android.permissioncontroller.permission.ui.model.AppPermissionViewModel.ButtonState
import com.android.permissioncontroller.permission.ui.model.AppPermissionViewModel.ButtonType
import com.android.permissioncontroller.permission.ui.v33.AdvancedConfirmDialogArgs
import com.android.permissioncontroller.permission.ui.wear.model.AppPermissionConfirmDialogViewModel
import com.android.permissioncontroller.permission.ui.wear.model.ConfirmDialogArgs
import com.android.permissioncontroller.wear.permission.components.ScrollableScreen
import com.android.permissioncontroller.wear.permission.components.material3.DialogButtonContent
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionConfirmationDialog
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionListFooter
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionTextProvider
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControl
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControlStyle
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControlType
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionMaterialUIVersion
import com.android.settingslib.RestrictedLockUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun WearAppPermissionScreen(
    title: String,
    viewModel: AppPermissionViewModel,
    confirmDialogViewModel: AppPermissionConfirmDialogViewModel,
    onLocationSwitchChanged: (Boolean) -> Unit,
    onGrantedStateChanged: (ButtonType, Boolean) -> Unit,
    onFooterClicked: (RestrictedLockUtils.EnforcedAdmin) -> Unit,
    onConfirmDialogOkButtonClick: (ConfirmDialogArgs) -> Unit,
    onConfirmDialogCancelButtonClick: () -> Unit,
    onAdvancedConfirmDialogOkButtonClick: (AdvancedConfirmDialogArgs) -> Unit,
    onAdvancedConfirmDialogCancelButtonClick: () -> Unit,
    onDisabledAllowButtonClick: () -> Unit,
) {
    val materialUIVersion = ResourceHelper.materialUIVersionInSettings
    val buttonState by
        remember(viewModel.buttonStateLiveData) {
                viewModel.buttonStateLiveData.asFlow().debounce(100L)
            }
            .collectAsStateWithLifecycle(null)
    val detailResIds by viewModel.detailResIdLiveData.observeAsState(null)
    val admin by viewModel.showAdminSupportLiveData.observeAsState(null)
    var isLoading by remember { mutableStateOf(true) }
    val showConfirmDialog by confirmDialogViewModel.showConfirmDialogLiveData.observeAsState(false)
    val showAdvancedConfirmDialog by
        confirmDialogViewModel.showAdvancedConfirmDialogLiveData.observeAsState(false)
    Box {
        WearAppPermissionContent(
            title,
            buttonState,
            detailResIds,
            admin,
            isLoading,
            onLocationSwitchChanged,
            onGrantedStateChanged,
            onFooterClicked,
            onDisabledAllowButtonClick,
        )
        ConfirmDialog(
            materialUIVersion = materialUIVersion,
            showDialog = showConfirmDialog,
            args = confirmDialogViewModel.confirmDialogArgs,
            onOkButtonClick = onConfirmDialogOkButtonClick,
            onCancelButtonClick = onConfirmDialogCancelButtonClick,
        )
        AdvancedConfirmDialog(
            materialUIVersion = materialUIVersion,
            showDialog = showAdvancedConfirmDialog,
            args = confirmDialogViewModel.advancedConfirmDialogArgs,
            onOkButtonClick = onAdvancedConfirmDialogOkButtonClick,
            onCancelButtonClick = onAdvancedConfirmDialogCancelButtonClick,
        )
    }
    if (isLoading && !buttonState.isNullOrEmpty()) {
        isLoading = false
    }
}

@Composable
internal fun WearAppPermissionContent(
    title: String,
    buttonState: Map<ButtonType, ButtonState>?,
    detailResIds: Pair<Int, Int?>?,
    admin: RestrictedLockUtils.EnforcedAdmin?,
    isLoading: Boolean,
    onLocationSwitchChanged: (Boolean) -> Unit,
    onGrantedStateChanged: (ButtonType, Boolean) -> Unit,
    onFooterClicked: (RestrictedLockUtils.EnforcedAdmin) -> Unit,
    onDisabledAllowButtonClick: () -> Unit,
) {
    ScrollableScreen(title = title, isLoading = isLoading) {
        buttonState?.get(ButtonType.LOCATION_ACCURACY)?.let {
            if (it.isShown) {
                item {
                    WearPermissionToggleControl(
                        checked = it.isChecked,
                        enabled = it.isEnabled,
                        label = stringResource(R.string.app_permission_location_accuracy),
                        toggleControl = WearPermissionToggleControlType.Switch,
                        onCheckedChanged = onLocationSwitchChanged,
                        labelMaxLines = Integer.MAX_VALUE,
                    )
                }
            }
        }
        for (buttonType in buttonTypeOrder) {
            buttonState?.get(buttonType)?.let {
                if (it.isShown) {
                    item {
                        WearPermissionToggleControl(
                            checked = it.isChecked,
                            style =
                                if (it.isEnabled) {
                                    WearPermissionToggleControlStyle.Default
                                } else {
                                    WearPermissionToggleControlStyle.DisabledLike
                                },
                            label = labelsByButton(buttonType),
                            toggleControl = WearPermissionToggleControlType.Radio,
                            onCheckedChanged = { checked ->
                                if (it.isEnabled) {
                                    onGrantedStateChanged(buttonType, checked)
                                } else {
                                    onDisabledAllowButtonClick()
                                }
                            },
                            labelMaxLines = Integer.MAX_VALUE,
                        )
                    }
                }
            }
        }
        detailResIds?.let {
            item {
                val infoButtonBuilder = remember {
                    WearPermissionIconBuilder.builder(R.drawable.ic_info)
                }
                WearPermissionListFooter(
                    label = WearPermissionTextProvider.Plain(stringResource(detailResIds.first)),
                    iconBuilder = infoButtonBuilder,
                    onClick = admin?.let { { onFooterClicked(admin) } },
                )
            }
        }
        buttonState?.get(ButtonType.ALLOW_FOR_COMPATIBILITY)?.let {
            if (it.isShown && it.isChecked) {
                item(key = "compatibility_footer") {
                    CompatibilityFooter(R.string.allow_for_compatibility_nearby_devices_footer)
                }
            }
        }
    }
}

internal val buttonTypeOrder =
    listOf(
        ButtonType.ALLOW,
        ButtonType.ALLOW_ALWAYS,
        ButtonType.ALLOW_FOREGROUND,
        ButtonType.ALLOW_FOR_COMPATIBILITY,
        ButtonType.ASK_ONCE,
        ButtonType.ASK,
        ButtonType.DENY,
        ButtonType.DENY_FOREGROUND,
    )

@Composable
internal fun labelsByButton(buttonType: ButtonType) =
    when (buttonType) {
        ButtonType.ALLOW -> stringResource(R.string.app_permission_button_allow)
        ButtonType.ALLOW_ALWAYS -> stringResource(R.string.app_permission_button_allow_always)
        ButtonType.ALLOW_FOREGROUND ->
            stringResource(R.string.app_permission_button_allow_foreground)
        ButtonType.ALLOW_FOR_COMPATIBILITY ->
            stringResource(R.string.app_permission_button_allow_for_compatibility)
        ButtonType.ASK_ONCE -> stringResource(R.string.app_permission_button_ask)
        ButtonType.ASK -> stringResource(R.string.app_permission_button_ask)
        ButtonType.DENY -> stringResource(R.string.app_permission_button_deny)
        ButtonType.DENY_FOREGROUND -> stringResource(R.string.app_permission_button_deny)
        else -> ""
    }

@Composable
internal fun ConfirmDialog(
    materialUIVersion: WearPermissionMaterialUIVersion,
    showDialog: Boolean,
    args: ConfirmDialogArgs?,
    onOkButtonClick: (ConfirmDialogArgs) -> Unit,
    onCancelButtonClick: () -> Unit,
) {
    args?.let {
        val positiveButton =
            remember(it, onOkButtonClick) { DialogButtonContent(onClick = { onOkButtonClick(it) }) }

        val negativeButton =
            remember(onCancelButtonClick) {
                DialogButtonContent(onClick = { onCancelButtonClick() })
            }

        WearPermissionConfirmationDialog(
            materialUIVersion = materialUIVersion,
            show = showDialog,
            message = stringResource(it.messageId),
            positiveButtonContent = positiveButton,
            negativeButtonContent = negativeButton,
        )
    }
}

@Composable
internal fun AdvancedConfirmDialog(
    materialUIVersion: WearPermissionMaterialUIVersion,
    showDialog: Boolean,
    args: AdvancedConfirmDialogArgs?,
    onOkButtonClick: (AdvancedConfirmDialogArgs) -> Unit,
    onCancelButtonClick: () -> Unit,
) {
    args?.let {
        val title =
            if (it.titleId != 0) {
                stringResource(it.titleId)
            } else {
                ""
            }

        val positiveText = stringResource(it.positiveButtonTextId)
        val negativeText = stringResource(it.negativeButtonTextId)

        val mainIcon = remember(it.iconId) { WearPermissionIconBuilder.builder(it.iconId) }

        val positiveButton =
            remember(it, onOkButtonClick, positiveText) {
                val iconBuilder =
                    WearPermissionIconBuilder.builder(Icons.Default.Check)
                        .contentDescription(positiveText)
                DialogButtonContent(icon = iconBuilder, onClick = { onOkButtonClick(it) })
            }

        val negativeButton =
            remember(onCancelButtonClick, negativeText) {
                val iconBuilder =
                    WearPermissionIconBuilder.builder(Icons.Default.Close)
                        .contentDescription(negativeText)
                DialogButtonContent(icon = iconBuilder, onClick = { onCancelButtonClick() })
            }

        WearPermissionConfirmationDialog(
            materialUIVersion = materialUIVersion,
            show = showDialog,
            title = title,
            iconRes = mainIcon,
            message = stringResource(it.messageId),
            positiveButtonContent = positiveButton,
            negativeButtonContent = negativeButton,
        )
    }
}
