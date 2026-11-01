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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.permissioncontroller.R
import com.android.permissioncontroller.role.UserPackage
import com.android.permissioncontroller.role.ui.ManageRoleHolderStateLiveData
import com.android.permissioncontroller.role.ui.RoleApplicationItem
import com.android.permissioncontroller.wear.permission.components.ScrollableScreen
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionButton
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionButtonStyle
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionListFooter
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionTextProvider
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControl
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControlStyle
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionToggleControlType
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionMaterialUIVersion

@Composable
fun WearRequestRoleScreen(
    helper: WearRequestRoleHelper,
    onSetAsDefault: (Boolean, UserPackage?) -> Unit,
    onCanceled: () -> Unit,
) {
    val roleLiveData = helper.viewModel.liveData.observeAsState(emptyList())
    val manageRoleHolderState =
        helper.viewModel.manageRoleHolderStateLiveData.observeAsState(
            ManageRoleHolderStateLiveData.STATE_WORKING
        )
    val dontAskAgain = helper.wearViewModel.dontAskAgain.observeAsState(false)
    val selectedPackage = helper.wearViewModel.selectedPackage.observeAsState(null)
    var isLoading by remember { mutableStateOf(true) }

    if (isLoading && roleLiveData.value.isNotEmpty()) {
        helper.initializeHolderPackage(roleLiveData.value)
        helper.initializeSelectedPackage()
    }

    val onCheckedChanged: (Boolean, UserPackage?, Boolean) -> Unit =
        { checked, userPackage, isHolder ->
            if (checked) {
                helper.wearViewModel.selectedPackage.value = userPackage
                helper.wearViewModel.isHolderChecked = isHolder
            }
        }

    val onDontAskAgainCheckedChanged: (Boolean) -> Unit = { checked ->
        helper.wearViewModel.dontAskAgain.value = checked
        if (checked) {
            helper.initializeSelectedPackage()
        }
    }
    WearRequestRoleContent(
        ResourceHelper.materialUIVersionInApp,
        isLoading,
        helper,
        roleLiveData.value,
        manageRoleHolderState.value == ManageRoleHolderStateLiveData.STATE_IDLE,
        dontAskAgain.value,
        selectedPackage.value,
        onCheckedChanged,
        onDontAskAgainCheckedChanged,
        onSetAsDefault,
        onCanceled,
    )

    if (isLoading && roleLiveData.value.isNotEmpty()) {
        isLoading = false
    }
}

@Composable
internal fun WearRequestRoleContent(
    materialUIVersion: WearPermissionMaterialUIVersion,
    isLoading: Boolean,
    helper: WearRequestRoleHelper,
    applicationItems: List<RoleApplicationItem>,
    enabled: Boolean,
    dontAskAgain: Boolean,
    selectedPackage: UserPackage?,
    onCheckedChanged: (Boolean, UserPackage?, Boolean) -> Unit,
    onDontAskAgainCheckedChanged: (Boolean) -> Unit,
    onSetAsDefault: (Boolean, UserPackage?) -> Unit,
    onCanceled: () -> Unit,
) {
    ScrollableScreen(
        materialUIVersion = materialUIVersion,
        imageBuilder = WearPermissionIconBuilder.builder(helper.getIcon()),
        title = helper.getTitle(),
        showTimeText = false,
        isLoading = isLoading,
    ) {
        helper.getNonePreference(applicationItems, selectedPackage)?.let { pref ->
            item {
                WearPermissionToggleControl(
                    materialUIVersion = materialUIVersion,
                    label = pref.label,
                    iconBuilder = pref.icon?.let { WearPermissionIconBuilder.builder(it) },
                    enabled = enabled && pref.enabled,
                    checked = pref.checked,
                    onCheckedChanged = { checked ->
                        onCheckedChanged(checked, pref.userPackage, pref.isHolder)
                    },
                    toggleControl = WearPermissionToggleControlType.Radio,
                    labelMaxLines = Integer.MAX_VALUE,
                )
            }
            pref.subTitle?.let { subTitle ->
                item {
                    WearPermissionListFooter(
                        materialUIVersion = materialUIVersion,
                        label = WearPermissionTextProvider.Plain(subTitle),
                    )
                }
            }
        }

        for (pref in helper.getPreferences(applicationItems, selectedPackage)) {
            item {
                WearPermissionToggleControl(
                    materialUIVersion = materialUIVersion,
                    label = pref.label,
                    iconBuilder = pref.icon?.let { WearPermissionIconBuilder.builder(it) },
                    enabled = enabled && pref.enabled,
                    checked = pref.checked,
                    onCheckedChanged = { checked ->
                        onCheckedChanged(checked, pref.userPackage, pref.isHolder)
                    },
                    toggleControl = WearPermissionToggleControlType.Radio,
                )
            }
            pref.subTitle?.let { subTitle ->
                item {
                    WearPermissionListFooter(
                        materialUIVersion = materialUIVersion,
                        label = WearPermissionTextProvider.Plain(subTitle),
                    )
                }
            }
        }

        if (helper.showDontAskButton()) {
            item {
                WearPermissionToggleControl(
                    materialUIVersion = materialUIVersion,
                    checked = dontAskAgain,
                    enabled = enabled,
                    onCheckedChanged = { checked -> run { onDontAskAgainCheckedChanged(checked) } },
                    label = stringResource(R.string.request_role_dont_ask_again),
                    toggleControl = WearPermissionToggleControlType.Checkbox,
                    style = WearPermissionToggleControlStyle.Transparent,
                    modifier =
                        Modifier.testTag("com.android.permissioncontroller:id/dont_ask_again"),
                )
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        item {
            WearPermissionButton(
                materialUIVersion = materialUIVersion,
                label = stringResource(R.string.request_role_set_as_default),
                style = WearPermissionButtonStyle.Primary,
                enabled = helper.shouldSetAsDefaultEnabled(enabled),
                onClick = { onSetAsDefault(dontAskAgain, selectedPackage) },
                modifier = Modifier.testTag("android:id/button1"),
            )
        }
        item {
            WearPermissionButton(
                materialUIVersion = materialUIVersion,
                label = stringResource(R.string.cancel),
                enabled = enabled,
                onClick = { onCanceled() },
                modifier = Modifier.testTag("android:id/button2"),
            )
        }
    }
}
