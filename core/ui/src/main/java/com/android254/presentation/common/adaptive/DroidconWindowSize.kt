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
package com.android254.presentation.common.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/** App-level layout intent, so breakpoints live in one place rather than in each screen. */
enum class DroidconWindowSize {
    /** < 600 dp. Single pane, bottom navigation bar. */
    Compact,

    /** 600–839 dp. Single pane, navigation rail. */
    Medium,

    /** >= 840 dp. Two panes, navigation drawer. */
    Expanded,
}

/** Reads the window, never the configuration, which is wrong in multi-window and mid-fold. */
@Composable
fun rememberDroidconWindowSize(): DroidconWindowSize {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    return remember(adaptiveInfo) {
        with(adaptiveInfo.windowSizeClass) {
            when {
                !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                    DroidconWindowSize.Compact

                !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
                    DroidconWindowSize.Medium

                else -> DroidconWindowSize.Expanded
            }
        }
    }
}

/**
 * Whether the window can hold two panes.
 *
 * Same directive the Material scene strategies use, so a screen cannot disagree with the
 * scaffold about whether its list is beside it. Not the same as [DroidconWindowSize.Medium].
 */
@Composable
fun rememberIsMultiPaneWindow(): Boolean {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    return remember(adaptiveInfo) {
        calculatePaneScaffoldDirective(adaptiveInfo).maxHorizontalPartitions > 1
    }
}

/** A drawer needs the width to hold it and the height not to be a phone on its side. */
@Composable
fun rememberShowsNavigationDrawer(): Boolean {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    return remember(adaptiveInfo) {
        with(adaptiveInfo.windowSizeClass) {
            isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
                isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
        }
    }
}

/** A bar drops its logo exactly when the drawer is there to take it, never merely when wide. */
@Composable
fun rememberShowsAppBarLogo(): Boolean = !rememberShowsNavigationDrawer()

/** Half open, hinge horizontal: content belongs above the fold, controls below it. */
@Composable
fun rememberIsTabletopPosture(): Boolean {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    return remember(adaptiveInfo) { adaptiveInfo.windowPosture.isTabletop }
}

/** Past this a line of body text stops being readable. */
val ReadablePaneMaxWidth: Dp = 840.dp

/**
 * Caps content at [ReadablePaneMaxWidth] and centres it.
 *
 * A layout modifier rather than `widthIn` plus an aligning parent, so it composes onto a lazy
 * list. The cap is read from the constant rather than taken as a parameter to keep the lambda
 * non-capturing: a capturing one is a fresh element per call, which invalidates measurement of
 * the scrolling subtree on every recomposition.
 */
fun Modifier.readablePaneWidth(): Modifier =
    layout { measurable, constraints ->
        val cap = ReadablePaneMaxWidth.roundToPx()
        if (!constraints.hasBoundedWidth) {
            val placeable = measurable.measure(constraints.copy(maxWidth = cap))
            return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
        }
        val width = minOf(constraints.maxWidth, cap)
        val placeable =
            measurable.measure(
                constraints.copy(minWidth = minOf(constraints.minWidth, width), maxWidth = width),
            )
        layout(constraints.maxWidth, placeable.height) {
            placeable.placeRelative((constraints.maxWidth - placeable.width) / 2, 0)
        }
    }