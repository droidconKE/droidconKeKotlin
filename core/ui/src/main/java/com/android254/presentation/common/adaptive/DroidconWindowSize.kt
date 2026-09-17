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

/**
 * App-level layout intent, derived from the window size class.
 *
 * Screens branch on this rather than on raw dp, so the breakpoints live in one place and a
 * screenshot test can force a value by overriding the window size rather than faking a device.
 */
enum class DroidconWindowSize {
    /** < 600 dp: phone portrait, small foldable closed. Single pane, bottom navigation bar. */
    Compact,

    /** 600–839 dp: tablet portrait, phone landscape, foldable open. Single pane, navigation rail. */
    Medium,

    /** >= 840 dp: tablet landscape, desktop, ChromeOS. Two panes, navigation drawer. */
    Expanded,
}

/**
 * The current window size class.
 *
 * Reads the window, never `Configuration.screenWidthDp` or `LocalConfiguration.orientation`:
 * both are wrong in multi-window, wrong on a foldable mid-fold, and wrong in a resizable
 * ChromeOS window.
 */
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
 * Whether the window can hold two panes side by side.
 *
 * Derived from the same [calculatePaneScaffoldDirective] the Material scene strategies use, so
 * a caller that hides a back arrow "because the list is beside us" cannot disagree with the
 * scaffold that decides whether the list actually is. Note this is **not** the same as
 * [DroidconWindowSize.Medium]: the standard directive allows a second pane only from 840 dp.
 */
@Composable
fun rememberIsMultiPaneWindow(): Boolean {
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    return remember(adaptiveInfo) {
        calculatePaneScaffoldDirective(adaptiveInfo).maxHorizontalPartitions > 1
    }
}

/**
 * Whether an app bar should draw the droidcon logo.
 *
 * At drawer sizes the drawer header carries the branding, and a logo in the bar as well is the
 * same mark twice on one screen. Lives here rather than in each bar so the three of them cannot
 * disagree, and is read as a default argument so no screen has to pass it.
 */
@Composable
fun rememberShowsAppBarLogo(): Boolean = rememberDroidconWindowSize() != DroidconWindowSize.Expanded

/** Past this, a line of body text stops being readable and the 20 dp gutters stop being a layout. */
val ReadablePaneMaxWidth: Dp = 840.dp

/**
 * Caps the content at [max] and centres it in whatever width it was given.
 *
 * A single pane stretched across a 1600 dp window is not a layout — it is a metre-long line of
 * text with a 20 dp gutter at each end. Written as a layout modifier rather than
 * `widthIn` + an aligning parent so it composes onto a lazy list, which cannot centre itself
 * through `contentPadding`.
 */
fun Modifier.readablePaneWidth(max: Dp = ReadablePaneMaxWidth): Modifier =
    layout { measurable, constraints ->
        if (!constraints.hasBoundedWidth) {
            val placeable = measurable.measure(constraints)
            return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
        }
        val width = minOf(constraints.maxWidth, max.roundToPx())
        val placeable =
            measurable.measure(
                constraints.copy(minWidth = minOf(constraints.minWidth, width), maxWidth = width),
            )
        layout(constraints.maxWidth, placeable.height) {
            placeable.placeRelative((constraints.maxWidth - placeable.width) / 2, 0)
        }
    }