/*
 * Copyright 2026 DroidconKE
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
package com.android254.presentation.common.insets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Who pays for which system inset. The split is what lets a screen draw under the status bar at
 * all: a root that consumes the top, as this one did, leaves the edge-to-edge opt-in inert.
 *
 * The root is `MainScreen`'s `NavigationSuiteScaffold`. It pays for whichever side its own
 * navigation component covers and **consumes exactly that side** — the bottom under a navigation
 * bar, the start under a rail or drawer, and nothing at all while the navigation is hidden. So
 * the same two declarations below are correct at every window size; what they resolve to moves
 * with the navigation component rather than needing a per-size branch.
 *
 * The one thing the root does not own is the bottom of the content area when something else is
 * sitting there: at rail and drawer sizes the live-sessions rail is the bottom-most element, so
 * it pads for the bottom inset and consumes it on the content's behalf.
 */
object DroidconWindowInsets {
    /**
     * The app bar's share, so its background reaches the status bar instead of stopping below it.
     *
     * The horizontal side is genuinely shared: beside a navigation rail the start inset is
     * already consumed, so this resolves to the end cutout only, which is what it should be.
     */
    val appBar: WindowInsets
        @Composable
        get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

    /**
     * A screen's share, for `contentWindowInsets`. No top — its app bar took that.
     *
     * The bottom is zero under a navigation bar or the live-sessions rail, because both consume
     * what they pay for, and is the navigation bar or the keyboard when neither is there — which
     * is the ordinary case beside a rail or a drawer, and on the three routes that hide the
     * navigation entirely.
     */
    val screenContent: WindowInsets
        @Composable
        get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
}

/**
 * This padding with a screen's own gutters added on top.
 *
 * A lazy list has one `contentPadding` and two things to put in it — the insets it must let its
 * content scroll under, and the gutters the design asks for. Adding them is the only way to keep
 * both; picking one is how the first item ends up under the status bar or hard against the edge.
 */
@Composable
fun PaddingValues.plus(
    horizontal: Dp = 0.dp,
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + horizontal,
        end = calculateEndPadding(layoutDirection) + horizontal,
        top = calculateTopPadding() + top,
        bottom = calculateBottomPadding() + bottom,
    )
}