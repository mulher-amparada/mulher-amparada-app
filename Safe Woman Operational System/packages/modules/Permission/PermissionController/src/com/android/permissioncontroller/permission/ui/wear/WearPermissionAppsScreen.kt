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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material.Text
import com.android.permissioncontroller.R
import com.android.permissioncontroller.permission.ui.Category
import com.android.permissioncontroller.wear.permission.components.ScrollableScreen
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionButton
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionListSubHeader

/** Compose the screen associated to a [WearPermissionAppsFragment]. */
@Composable
fun WearPermissionAppsScreen(helper: WearPermissionAppsHelper) {
    val categorizedApps by helper.categorizedAppsLiveData().observeAsState(emptyMap())
    val hasSystemApps by helper.hasSystemAppsLiveData().observeAsState(false)
    val showSystem by helper.shouldShowSystemLiveData().observeAsState(false)
    val appPermissionUsages by helper.wearViewModel.appPermissionUsages.observeAsState(emptyList())
    val showLocationProviderDialog by
        helper.locationProviderDialogViewModel.dialogVisibilityLiveData.observeAsState(false)
    val dialogArgs by
        helper.locationProviderDialogViewModel.locationProviderInterceptDialogArgs.observeAsState(
            null
        )

    var isLoading by remember { mutableStateOf(true) }
    val chipsByCategory by
        remember(categorizedApps, appPermissionUsages) {
            derivedStateOf { helper.getChipsByCategory(categorizedApps, appPermissionUsages) }
        }
    LaunchedEffect(Unit) { helper.setCreationLogged(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        WearPermissionAppsContent(
            chipsByCategory = chipsByCategory,
            showSystem = showSystem,
            hasSystemApps = hasSystemApps,
            title = helper.getTitle(),
            subtitle = helper.getSubTitle(),
            showAlways = helper.showAlways(),
            isLoading = isLoading,
            onShowSystemClick = helper.onShowSystemClick,
        )
        LocationProviderDialogScreen(
            showDialog = showLocationProviderDialog,
            onDismissRequest = { helper.locationProviderDialogViewModel.dismissDialog() },
            args = dialogArgs,
        )
    }
    if (isLoading && categorizedApps.isNotEmpty()) {
        isLoading = false
    }
}

@Composable
internal fun WearPermissionAppsContent(
    chipsByCategory: Map<String, List<ChipInfo>>,
    showSystem: Boolean,
    hasSystemApps: Boolean,
    title: String,
    subtitle: String,
    showAlways: Boolean,
    isLoading: Boolean,
    onShowSystemClick: (showSystem: Boolean) -> Unit,
) {
    ScrollableScreen(title = title, subtitle = subtitle, isLoading = isLoading) {
        val firstVisibleCategory =
            categoryOrder.firstOrNull { !chipsByCategory[it].isNullOrEmpty() }
        categoryOrder.forEach { category ->
            val chips = chipsByCategory[category] ?: emptyList()
            if (chips.isNotEmpty()) {
                item(key = "header_$category") {
                    WearPermissionListSubHeader(
                        isFirstItemInAList = category == firstVisibleCategory
                    ) {
                        Text(text = stringResource(getCategoryString(category, showAlways)))
                    }
                }

                items(chips.count()) { index ->
                    val chip = chips[index]
                    WearPermissionButton(
                        label = chip.title,
                        labelMaxLines = Int.MAX_VALUE,
                        secondaryLabel = chip.summary,
                        secondaryLabelMaxLines = Int.MAX_VALUE,
                        iconBuilder = chip.icon?.let { WearPermissionIconBuilder.builder(it) },
                        enabled = chip.enabled,
                        onClick = chip.onClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (hasSystemApps) {
            item(key = "system_toggle") {
                WearPermissionButton(
                    label =
                        stringResource(
                            if (showSystem) R.string.menu_hide_system else R.string.menu_show_system
                        ),
                    onClick = { onShowSystemClick(!showSystem) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        val compatibilityItems = chipsByCategory[Category.ALLOWED_FOR_COMPATIBILITY.categoryName]
        if (!compatibilityItems.isNullOrEmpty()) {
            item(key = "compatibility_footer") { CompatibilityFooter() }
        }
    }
}

internal fun getCategoryString(category: String, showAlways: Boolean) =
    when (category) {
        "allowed_storage_full" -> R.string.allowed_storage_full
        "allowed_storage_scoped" -> R.string.allowed_storage_scoped
        Category.ALLOWED.categoryName ->
            if (showAlways) {
                R.string.allowed_always_header
            } else {
                R.string.allowed_header
            }

        Category.ALLOWED_FOR_COMPATIBILITY.categoryName -> R.string.allowed_for_compatibility_header
        Category.ALLOWED_FOREGROUND.categoryName -> R.string.allowed_foreground_header
        Category.ASK.categoryName -> R.string.ask_header
        Category.DENIED.categoryName -> R.string.denied_header
        else -> throw IllegalArgumentException("Wrong category: $category")
    }

internal val categoryOrder =
    listOf(
        "allowed_storage_full",
        "allowed_storage_scoped",
        Category.ALLOWED.categoryName,
        Category.ALLOWED_FOREGROUND.categoryName,
        Category.ASK.categoryName,
        Category.ALLOWED_FOR_COMPATIBILITY.categoryName,
        Category.DENIED.categoryName,
    )
