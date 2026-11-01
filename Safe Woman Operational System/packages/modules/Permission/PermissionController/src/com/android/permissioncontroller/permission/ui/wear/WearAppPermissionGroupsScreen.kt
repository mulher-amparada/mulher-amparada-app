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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.android.permissioncontroller.R
import com.android.permissioncontroller.permission.model.v31.AppPermissionUsage
import com.android.permissioncontroller.permission.ui.wear.model.RevokeDialogArgs
import com.android.permissioncontroller.wear.permission.components.ScrollableScreen
import com.android.permissioncontroller.wear.permission.components.material3.DialogButtonContent
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionButton
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionConfirmationDialog
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControl
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControlType
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionMaterialUIVersion

@Composable
fun WearAppPermissionGroupsScreen(helper: WearAppPermissionGroupsHelper) {
    val loadingSentinel = remember { emptyList<AppPermissionUsage>() }
    val materialUIVersion = ResourceHelper.materialUIVersionInSettings
    val packagePermGroups by helper.viewModel.packagePermGroupsLiveData.observeAsState(null)
    val autoRevoke by helper.viewModel.autoRevokeLiveData.observeAsState(null)
    val appPermissionUsages by
        helper.wearViewModel.appPermissionUsages.observeAsState(loadingSentinel)
    val showRevokeDialog by helper.revokeDialogViewModel.showDialogLiveData.observeAsState(false)
    val showLocationProviderDialog by
        helper.locationProviderInterceptDialogViewModel.dialogVisibilityLiveData.observeAsState(
            false
        )
    val locationProviderDialogArgs by
        helper.locationProviderInterceptDialogViewModel.locationProviderInterceptDialogArgs
            .observeAsState(null)

    val groupChips =
        remember(appPermissionUsages) { helper.getPermissionGroupChipParams(appPermissionUsages) }
    var isLoading by remember { mutableStateOf(true) }

    Box {
        WearAppPermissionGroupsContent(
            isLoading = isLoading,
            permissionGroupChipParams = groupChips,
            autoRevokeChipParam = helper.getAutoRevokeChipParam(autoRevoke),
            isCompatibilityFooterRequired = helper.isCompatibilityFooterRequired(groupChips),
        )
        RevokeDialog(
            materialUIVersion = materialUIVersion,
            showDialog = showRevokeDialog,
            args = helper.revokeDialogViewModel.revokeDialogArgs,
        )
        LocationProviderDialogScreen(
            showDialog = showLocationProviderDialog,
            onDismissRequest = { helper.locationProviderInterceptDialogViewModel.dismissDialog() },
            args = locationProviderDialogArgs,
        )
    }

    if (
        isLoading && !packagePermGroups.isNullOrEmpty() && appPermissionUsages !== loadingSentinel
    ) {
        isLoading = false
    }
}

@Composable
internal fun WearAppPermissionGroupsContent(
    isLoading: Boolean,
    permissionGroupChipParams: List<PermissionGroupChipParam>,
    autoRevokeChipParam: AutoRevokeChipParam?,
    isCompatibilityFooterRequired: Boolean,
) {
    ScrollableScreen(title = stringResource(R.string.app_permissions), isLoading = isLoading) {
        if (permissionGroupChipParams.isEmpty()) {
            item {
                WearPermissionButton(label = stringResource(R.string.no_permissions), onClick = {})
            }
        } else {
            for (info in permissionGroupChipParams) {
                item {
                    if (info.checked != null) {
                        WearPermissionToggleControl(
                            toggleControl = WearPermissionToggleControlType.Switch,
                            label = info.label,
                            checked = info.checked,
                            enabled = info.enabled,
                            onCheckedChanged = info.onCheckedChanged,
                        )
                    } else {
                        WearPermissionButton(
                            label = info.label,
                            labelMaxLines = Integer.MAX_VALUE,
                            secondaryLabel = info.summary,
                            secondaryLabelMaxLines = Integer.MAX_VALUE,
                            enabled = info.enabled,
                            onClick = info.onClick,
                        )
                    }
                }
            }
            autoRevokeChipParam?.let {
                if (it.visible) {
                    item {
                        WearPermissionToggleControl(
                            checked = it.checked,
                            label = stringResource(it.labelRes),
                            labelMaxLines = 3,
                            toggleControl = WearPermissionToggleControlType.Switch,
                            onCheckedChanged = it.onCheckedChanged,
                        )
                    }
                }
            }
            if (isCompatibilityFooterRequired) {
                item { CompatibilityFooter() }
            }
        }
    }
}

@Composable
internal fun RevokeDialog(
    materialUIVersion: WearPermissionMaterialUIVersion,
    showDialog: Boolean,
    args: RevokeDialogArgs?,
) {

    args?.run {
        WearPermissionConfirmationDialog(
            materialUIVersion = materialUIVersion,
            show = showDialog,
            message = stringResource(messageId),
            positiveButtonContent = DialogButtonContent(onClick = onOkButtonClick),
            negativeButtonContent = DialogButtonContent(onClick = onCancelButtonClick),
        )
    }
}
