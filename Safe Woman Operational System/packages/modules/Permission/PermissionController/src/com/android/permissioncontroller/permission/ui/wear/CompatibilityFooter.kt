/*
 * Copyright (C) 2026 The Android Open Source Project
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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.IconButtonDefaults
import com.android.permissioncontroller.R
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionListFooter
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionTextProvider

@Composable
fun CompatibilityFooter(
    footerLabelId: Int = R.string.allowed_for_compatibility_nearby_devices_footer
) {
    val footerText = stringResource(footerLabelId)
    val compatibilityFooterString = remember(footerText) { AnnotatedString.fromHtml(footerText) }
    val iconBuilder = remember {
        WearPermissionIconBuilder.builder(R.drawable.ic_info_outline)
            .modifier(Modifier.size(IconButtonDefaults.SmallIconSize))
    }
    WearPermissionListFooter(
        label = WearPermissionTextProvider.AnnotatedHtml(compatibilityFooterString),
        iconBuilder = iconBuilder,
        modifier = Modifier.padding(top = 12.dp),
    )
}
