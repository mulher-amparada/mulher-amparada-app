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
package com.android.permissioncontroller.wear.permission.components.material3

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.LocalTextConfiguration
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.android.permissioncontroller.wear.permission.components.R
import com.android.permissioncontroller.wear.permission.components.material2.ToggleChip
import com.android.permissioncontroller.wear.permission.components.theme.LocalCustomDimensions
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionMaterialUIVersion

/** Defines various toggle control types. */
enum class WearPermissionToggleControlType {
    Switch,
    Radio,
    Checkbox,
}

/**
 * The custom component is a wrapper on different material3 toggle controls.
 * 1. It provides an unified interface for RadioButton,CheckButton and SwitchButton.
 * 2. It takes icon, primary, secondary label resources and construct them applying permission app
 *    defaults
 * 3. Applies custom semantics for based on the toggle control type
 */
@Composable
fun WearPermissionToggleControl(
    toggleControl: WearPermissionToggleControlType,
    label: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    labelMaxLines: Int? = null,
    materialUIVersion: WearPermissionMaterialUIVersion = ResourceHelper.materialUIVersionInSettings,
    iconBuilder: WearPermissionIconBuilder? = null,
    secondaryLabel: String? = null,
    secondaryLabelMaxLines: Int? = null,
    enabled: Boolean = true,
    style: WearPermissionToggleControlStyle = WearPermissionToggleControlStyle.Default,
) {
    if (materialUIVersion == WearPermissionMaterialUIVersion.MATERIAL2_5) {
        ToggleChip(
            toggleControl = toggleControl,
            label = label,
            labelMaxLine = labelMaxLines,
            checked = checked,
            onCheckedChanged = onCheckedChanged,
            modifier = modifier,
            icon = iconBuilder?.iconResource,
            secondaryLabel = secondaryLabel,
            secondaryLabelMaxLine = secondaryLabelMaxLines,
            enabled = enabled,
            colors = style.material2ToggleControlColors(),
        )
    } else {
        WearPermissionToggleControlInternal(
            label = label,
            toggleControl = toggleControl,
            checked = checked,
            onCheckedChanged = onCheckedChanged,
            modifier = modifier,
            iconBuilder = iconBuilder,
            labelMaxLines = labelMaxLines,
            secondaryLabel = secondaryLabel,
            secondaryLabelMaxLines = secondaryLabelMaxLines,
            enabled = enabled,
            style = style,
        )
    }
}

@Composable
private fun WearPermissionToggleControlInternal(
    label: String,
    toggleControl: WearPermissionToggleControlType,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconBuilder: WearPermissionIconBuilder? = null,
    labelMaxLines: Int? = null,
    secondaryLabel: String? = null,
    secondaryLabelMaxLines: Int? = null,
    enabled: Boolean = true,
    style: WearPermissionToggleControlStyle = WearPermissionToggleControlStyle.Default,
) {
    val labelParam: (@Composable RowScope.() -> Unit) = {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            maxLines = labelMaxLines ?: LocalTextConfiguration.current.maxLines,
            style = LocalTextStyle.current.copy(hyphens = Hyphens.Auto),
        )
    }

    val secondaryLabelParam: (@Composable RowScope.() -> Unit)? =
        secondaryLabel?.let {
            {
                Text(
                    text = it,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = secondaryLabelMaxLines ?: LocalTextConfiguration.current.maxLines,
                    style = LocalTextStyle.current.copy(hyphens = Hyphens.Auto),
                )
            }
        }

    val iconParam: (@Composable BoxScope.() -> Unit)? = iconBuilder?.let { { it.build() } }
    val toggleControlStateDescription =
        stringResource(
            if (checked) {
                R.string.on
            } else {
                R.string.off
            }
        )
    val updatedModifier =
        modifier
            .requiredSizeIn(minHeight = LocalCustomDimensions.current.toggleMinHeight.dp)
            .fillMaxWidth()
            .semantics { stateDescription = toggleControlStateDescription }

    when (toggleControl) {
        WearPermissionToggleControlType.Radio ->
            RadioButton(
                selected = checked,
                onSelect = {
                    // We do not want to call if it is already checked.
                    // Radio button can't be toggled off
                    if (!checked) {
                        onCheckedChanged(true)
                    }
                },
                modifier = updatedModifier,
                enabled = enabled,
                icon = iconParam,
                secondaryLabel = secondaryLabelParam,
                label = labelParam,
                colors = style.radioButtonColorScheme(),
            )

        WearPermissionToggleControlType.Checkbox ->
            CheckboxButton(
                checked = checked,
                onCheckedChange = onCheckedChanged,
                modifier = updatedModifier,
                enabled = enabled,
                icon = iconParam,
                secondaryLabel = secondaryLabelParam,
                label = labelParam,
                colors = style.checkboxColorScheme(),
            )

        WearPermissionToggleControlType.Switch ->
            SwitchButton(
                checked = checked,
                onCheckedChange = onCheckedChanged,
                modifier = updatedModifier,
                enabled = enabled,
                icon = iconParam,
                secondaryLabel = secondaryLabelParam,
                label = labelParam,
                colors = style.switchButtonColorScheme(),
            )
    }
}
