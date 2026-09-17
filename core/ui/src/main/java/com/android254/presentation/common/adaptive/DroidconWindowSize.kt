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

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass

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
 * Whether the window is wide enough that panes are on the table at all.
 *
 * A decision about furniture — whether a detail keeps the navigation area, whether a supporting
 * entry is worth appending. Whether a pane is actually laid out is [rememberContentPaneDirective],
 * which measures the space left over; this one must stay window-level or the two feed each other.
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

/**
 * The pane directive for [contentSize] rather than for the whole window.
 *
 * The navigation component lives inside the window, so a drawer takes 360 dp of it before content
 * is measured. Sizing panes from the window then lays two of them out in what is left: on an
 * 841 dp foldable that squeezed the sessions list to 20 dp beside its own placeholder.
 */
@Composable
fun rememberContentPaneDirective(contentSize: DpSize): PaneScaffoldDirective {
    val posture = currentWindowAdaptiveInfoV2().windowPosture
    return remember(contentSize, posture) {
        calculatePaneScaffoldDirective(
            WindowAdaptiveInfo(
                windowSizeClass =
                    WindowSizeClass.BREAKPOINTS_V2.computeWindowSizeClass(
                        widthDp = contentSize.width.value,
                        heightDp = contentSize.height.value,
                    ),
                windowPosture = posture,
            ),
        )
    }
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