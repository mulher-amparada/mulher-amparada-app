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

package com.android.permissioncontroller.permission.ui.wear

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.LocalContentColor
import com.android.permissioncontroller.permission.ui.wear.model.LocationProviderInterceptDialogArgs
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionButton
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionButtonStyle
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionScaffold

@Composable
fun LocationProviderDialogScreen(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    args: LocationProviderInterceptDialogArgs?,
) {
    Dialog(visible = showDialog, onDismissRequest = onDismissRequest) {
        val tintColor = LocalContentColor.current
        args?.run {
            WearPermissionScaffold(
                showTimeText = false,
                imageBuilder = WearPermissionIconBuilder.builder(iconId).tint(tintColor),
                title = stringResource(titleId),
                subtitle = message,
                isLoading = false,
                content = {
                    item {
                        WearPermissionButton(
                            label = stringResource(locationSettingsId),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onLocationSettingsClick,
                            style = WearPermissionButtonStyle.Primary,
                        )
                    }
                    item {
                        WearPermissionButton(
                            label = stringResource(okButtonTitleId),
                            onClick = onOkButtonClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
            )
        }
    }
}
