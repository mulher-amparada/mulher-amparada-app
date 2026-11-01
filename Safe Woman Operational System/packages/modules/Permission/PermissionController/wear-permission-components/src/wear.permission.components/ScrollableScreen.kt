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
package com.android.permissioncontroller.wear.permission.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.wear.compose.foundation.ExpandableState
import androidx.wear.compose.foundation.SwipeToDismissValue
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.SwipeToDismissBox
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionScaffold
import com.android.permissioncontroller.wear.permission.components.theme.ResourceHelper
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionMaterialUIVersion

/**
 * Screen that contains a list of items defined using the [content] parameter, adds the time text
 * (if [showTimeText] is true), the tile (if [title] is not null), the vignette and the position
 * indicator. It also manages the scaling animation and allows the user to scroll the content using
 * the crown.
 */
@Composable
fun ScrollableScreen(
    materialUIVersion: WearPermissionMaterialUIVersion = ResourceHelper.materialUIVersionInSettings,
    asScalingList: Boolean = true,
    showTimeText: Boolean = true,
    title: String? = null,
    subtitle: CharSequence? = null,
    imageBuilder: WearPermissionIconBuilder? = null,
    isLoading: Boolean = false,
    titleTestTag: String? = null,
    subtitleTestTag: String? = null,
    content: ListScopeWrapper.() -> Unit,
) {
    var dismissed by remember { mutableStateOf(false) }
    val activity = LocalContext.current.findActivity()
    val state = rememberSwipeToDismissBoxState()

    LaunchedEffect(state.currentValue) {
        // If the swipe is complete
        if (state.currentValue == SwipeToDismissValue.Dismissed) {
            // pop the top fragment immediately or dismiss activity.
            dismiss(activity)
            // Set  dismissed state as true
            dismissed = true
            // Set swipe box back to starting position(that is cancelled swipe effect) to
            // show loading indicator while fragment dismisses.
            // For some reason fragment `popBackImmediate` takes few secs at times.
            state.snapTo(SwipeToDismissValue.Default)
        }
    }

    if (getBackStackEntryCount(activity) > 0) {
        SwipeToDismissBox(state = state) { isBackground ->
            WearPermissionScaffold(
                materialUIVersion,
                asScalingList,
                showTimeText,
                title,
                subtitle,
                imageBuilder,
                isLoading = isLoading || isBackground || dismissed,
                content,
                titleTestTag,
                subtitleTestTag,
            )
        }
    } else {
        WearPermissionScaffold(
            materialUIVersion,
            asScalingList,
            showTimeText,
            title,
            subtitle,
            imageBuilder,
            isLoading,
            content,
            titleTestTag,
            subtitleTestTag,
        )
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

internal fun getBackStackEntryCount(activity: Activity): Int {
    return if (activity is FragmentActivity) {
        activity.supportFragmentManager.primaryNavigationFragment
            ?.childFragmentManager
            ?.backStackEntryCount ?: 0
    } else {
        0
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("The screen should be called in the context of an Activity")
}

interface ListScopeWrapper {
    fun item(key: Any? = null, contentType: Any? = null, content: @Composable () -> Unit)

    fun items(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        content: @Composable (index: Int) -> Unit,
    )

    fun expandableItems(
        state: ExpandableState,
        count: Int,
        key: ((index: Int) -> Any)? = null,
        itemContent: @Composable BoxScope.(index: Int) -> Unit,
    )

    fun expandableButton(state: ExpandableState, key: Any? = null, content: @Composable () -> Unit)
}
