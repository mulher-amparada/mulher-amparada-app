/*
 * Copyright 2025 The Android Open Source Project
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
package com.android.permissioncontroller.wear.permission.components.material2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import com.android.permissioncontroller.wear.permission.components.AnnotatedText
import com.android.permissioncontroller.wear.permission.components.material3.WearPermissionIconBuilder
import com.android.permissioncontroller.wear.permission.components.theme.WearPermissionTheme

/**
 * This component is wrapper on material 2 scaffold component. It helps with time text, scroll
 * indicator and standard list elements like title, icon and subtitle.
 */
@Composable
fun Wear2Scaffold(
    showTimeText: Boolean,
    title: String?,
    subtitle: CharSequence?,
    imageBuilder: WearPermissionIconBuilder?,
    isLoading: Boolean,
    content: ScalingLazyListScope.() -> Unit,
    titleTestTag: String? = null,
    subtitleTestTag: String? = null,
) {
    val itemsSpacedBy = 4.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val scrollContentHorizontalPadding = (screenWidth * 0.052).dp
    val titleHorizontalPadding = (screenWidth * 0.0884).dp
    val subtitleHorizontalPadding = (screenWidth * 0.0416).dp
    val scrollContentTopPadding = (screenHeight * 0.1456).dp - itemsSpacedBy
    val scrollContentBottomPadding = (screenHeight * 0.3636).dp
    val titleBottomPadding =
        if (subtitle == null) {
            8.dp
        } else {
            4.dp
        }
    val subtitleBottomPadding = 8.dp
    val timeTextTopPadding =
        if (showTimeText) {
            1.dp
        } else {
            0.dp
        }
    val titlePaddingValues =
        PaddingValues(
            start = titleHorizontalPadding,
            top = 4.dp,
            bottom = titleBottomPadding,
            end = titleHorizontalPadding,
        )
    val subTitlePaddingValues =
        PaddingValues(
            start = subtitleHorizontalPadding,
            top = 4.dp,
            bottom = subtitleBottomPadding,
            end = subtitleHorizontalPadding,
        )
    val initialCenterIndex = 0
    val centerHeightDp = Dp(LocalConfiguration.current.screenHeightDp / 2.0f)
    // We are adding TimeText's padding to create a smooth scrolling
    val initialCenterItemScrollOffset = scrollContentTopPadding + timeTextTopPadding
    val scrollAwayOffset = centerHeightDp - initialCenterItemScrollOffset
    val focusRequester = remember { FocusRequester() }
    val listState = remember { ScalingLazyListState(initialCenterItemIndex = initialCenterIndex) }
    LaunchedEffect(title) {
        listState.animateScrollToItem(index = 0) // Scroll to the top when triggerValue changes
    }
    WearPermissionTheme {
        Scaffold(
            modifier = Modifier.focusRequester(focusRequester),
            timeText = {
                if (showTimeText && !isLoading) {
                    TimeText(
                        modifier =
                            Modifier.scrollAway(listState, initialCenterIndex, scrollAwayOffset)
                                .padding(top = timeTextTopPadding)
                    )
                }
            },
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            positionIndicator =
                if (!isLoading) {
                    { PositionIndicator(scalingLazyListState = listState) }
                } else {
                    null
                },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    ScalingLazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = listState,
                        // Set autoCentering to null to avoid adding extra padding based on the
                        // content.
                        autoCentering = null,
                        contentPadding =
                            PaddingValues(
                                start = scrollContentHorizontalPadding,
                                end = scrollContentHorizontalPadding,
                                top = scrollContentTopPadding,
                                bottom = scrollContentBottomPadding,
                            ),
                    ) {
                        staticItem()
                        imageBuilder?.modifier(Modifier.size(24.dp))?.let {
                            item { it.buildAsImage() }
                        }
                        if (title != null) {
                            item {
                                var modifier: Modifier = Modifier
                                if (titleTestTag != null) {
                                    modifier = modifier.testTag(titleTestTag)
                                }
                                ListHeader(modifier = Modifier.padding(titlePaddingValues)) {
                                    Text(
                                        text = title,
                                        textAlign = TextAlign.Center,
                                        modifier = modifier,
                                    )
                                }
                            }
                        }
                        if (subtitle != null) {
                            item {
                                var modifier: Modifier =
                                    Modifier.align(Alignment.Center).padding(subTitlePaddingValues)
                                if (subtitleTestTag != null) {
                                    modifier = modifier.testTag(subtitleTestTag)
                                }
                                AnnotatedText(
                                    text = subtitle,
                                    style =
                                        MaterialTheme.typography.body2.copy(
                                            color = MaterialTheme.colors.onSurfaceVariant
                                        ),
                                    modifier = modifier,
                                    shouldCapitalize = true,
                                )
                            }
                        }

                        content()
                    }
                    RequestFocusOnResume(focusRequester = focusRequester)
                }
            }
        }
    }
}

private fun ScalingLazyListScope.staticItem() {
    /*
    This empty item helps to ensure accurate scroll offset calculation. If auto centering is enabled
    initial item's(first item for us) center matches the center of the screen. Scroll offset is 0 at
    that point.

    if auto centering is not enabled, initial item will start at the top of the screen with the
    scroll offset equal to ScreenHeight/2 - scrollContentTopPadding - firstItemHeight/2.

    We need to this offset value to properly move time text.That is the scroll-away offset of the
    Time Text is equal to the scroll offset of the list at initial position.

    It is easier to calculate if we know the values of ScreenHeight, ScrollContentTopPadding and
    FirstItem's height. ScreenHeight and ScrollContentPadding are constants but height of the
    FirstItem depends on the content. Instead of measuring the height, we can simplify the
    calculation with an empty item with 0dp height.
    */
    item {}
}

@Composable
private fun RequestFocusOnResume(focusRequester: FocusRequester) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            focusRequester.requestFocus()
        }
    }
}
