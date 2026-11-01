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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ListSubHeader
import com.android.permissioncontroller.wear.permission.components.material2.ListSubheader
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionMaterialUIVersion

/*
This component is simplified wrapper over ListSubHeader with quick padding adjustments
 */
@Composable
fun WearPermissionListSubHeader(
    wearPermissionMaterialUIVersion: WearPermissionMaterialUIVersion =
        ResourceHelper.materialUIVersionInSettings,
    isFirstItemInAList: Boolean,
    label: @Composable RowScope.() -> Unit,
) {
    val paddingDefaults = rememberPaddingDefaults()
    val subtitlePaddingDefaults =
        remember(paddingDefaults, isFirstItemInAList) {
            paddingDefaults.subHeaderPaddingValues(needsLargePadding = !isFirstItemInAList)
        }

    if (wearPermissionMaterialUIVersion == WearPermissionMaterialUIVersion.MATERIAL3) {
        ListSubHeader(
            modifier = Modifier.requiredHeightIn(1.dp), // We do not want default min height
            contentPadding = subtitlePaddingDefaults,
            label = label,
        )
    } else {
        ListSubheader(modifier = Modifier.padding(subtitlePaddingDefaults), label = label)
    }
}
