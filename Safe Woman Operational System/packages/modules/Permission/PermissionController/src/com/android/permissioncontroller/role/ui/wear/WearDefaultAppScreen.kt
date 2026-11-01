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

package com.android.permissioncontroller.role.ui.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import com.android.permission.flags.Flags
import com.android.permissioncontroller.role.ui.RoleApplicationItem
import com.android.permissioncontroller.role.ui.wear.model.ConfirmDialogArgs
import com.android.permissioncontroller.wear.permission.components.ScrollableScreen
import com.android.permissioncontroller.wear.permission.components.material3.DialogButtonContent
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionConfirmationDialog
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionListFooter
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionListSubHeader
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionTextProvider
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControl
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControlStyle
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControlType
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionMaterialUIVersion
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun WearDefaultAppScreen(helper: WearDefaultAppHelper) {

    val roleLiveData by
        remember(helper.viewModel.liveData) { helper.viewModel.liveData.asFlow().debounce(100) }
            .collectAsStateWithLifecycle(initialValue = emptyList())

    val recommendedRoleLiveData by
        remember(helper.viewModel.recommendedLiveData) {
                helper.viewModel.recommendedLiveData.asFlow().debounce(100)
            }
            .collectAsStateWithLifecycle(initialValue = emptyList())

    val showConfirmDialog by
        helper.confirmDialogViewModel.showConfirmDialogLiveData.observeAsState(false)
    var isLoading by remember { mutableStateOf(true) }
    val materialUIVersion = ResourceHelper.materialUIVersionInSettings
    Box {
        WearDefaultAppContent(isLoading, recommendedRoleLiveData, roleLiveData, helper)
        ConfirmDialog(
            materialUIVersion = materialUIVersion,
            showDialog = showConfirmDialog,
            args = helper.confirmDialogViewModel.confirmDialogArgs,
        )
    }
    if (isLoading && (roleLiveData.isNotEmpty() || recommendedRoleLiveData.isNotEmpty())) {
        isLoading = false
    }
}

@Composable
private fun WearDefaultAppContent(
    isLoading: Boolean,
    recommendedItems: List<RoleApplicationItem>,
    otherItems: List<RoleApplicationItem>,
    helper: WearDefaultAppHelper,
) {
    val nonePref = helper.getNonePreference(recommendedItems, otherItems)
    ScrollableScreen(title = helper.getTitle(), isLoading = isLoading) {
        if (Flags.defaultAppsRecommendationEnabled() && recommendedItems.isNotEmpty()) {
            helper.recommendedItemTitle()?.let {
                item { WearPermissionListSubHeader(isFirstItemInAList = true) { Text(text = it) } }
            }
            for (pref in helper.getPreferences(recommendedItems)) {
                item { RoleApplicationEntryToggle(pref) }
            }
            helper.recommendedItemDescription()?.let {
                item { WearPermissionListFooter(label = WearPermissionTextProvider.Plain(it)) }
            }
            // Add other items title if other items is not empty.
            if (nonePref != null || otherItems.isNotEmpty()) {
                helper.otherItemsTitle()?.let {
                    item {
                        WearPermissionListSubHeader(isFirstItemInAList = false) { Text(text = it) }
                    }
                }
            }
        }
        nonePref?.run { item { NoneEntryToggle(this) } }
        for (pref in helper.getPreferences(otherItems)) {
            item { RoleApplicationEntryToggle(pref) }
        }
        item {
            WearPermissionListFooter(
                label = WearPermissionTextProvider.Plain(helper.getDescription())
            )
        }
    }
}

@Composable
private fun NoneEntryToggle(pref: WearRoleApplicationPreference) =
    WearPermissionToggleControl(
        label = pref.title.toString(),
        iconBuilder = pref.icon?.let { WearPermissionIconBuilder.builder(it) },
        checked = pref.checked,
        onCheckedChanged = pref.onDefaultCheckChanged,
        toggleControl = WearPermissionToggleControlType.Radio,
        labelMaxLines = Integer.MAX_VALUE,
    )

@Composable
private fun RoleApplicationEntryToggle(pref: WearRoleApplicationPreference) =
    WearPermissionToggleControl(
        label = pref.title.toString(),
        iconBuilder = pref.icon?.let { WearPermissionIconBuilder.builder(it) },
        style =
            if (pref.isEnabled) {
                WearPermissionToggleControlStyle.Default
            } else {
                WearPermissionToggleControlStyle.DisabledLike
            },
        secondaryLabel = pref.summary?.toString(),
        checked = pref.checked,
        onCheckedChanged = pref.getOnCheckChanged(),
        toggleControl = WearPermissionToggleControlType.Radio,
        labelMaxLines = Integer.MAX_VALUE,
        secondaryLabelMaxLines = Integer.MAX_VALUE,
    )

@Composable
private fun ConfirmDialog(
    materialUIVersion: WearPermissionMaterialUIVersion,
    showDialog: Boolean,
    args: ConfirmDialogArgs?,
) {
    args?.run {
        WearPermissionConfirmationDialog(
            materialUIVersion = materialUIVersion,
            show = showDialog,
            title = info.title?.toString(),
            message = info.message.toString(),
            positiveButtonContent = DialogButtonContent(onClick = onOkButtonClick),
            negativeButtonContent = DialogButtonContent(onClick = onCancelButtonClick),
        )
    }
}
