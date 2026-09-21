/*
 * Copyright 2022 DroidconKE
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
package com.android254.presentation.common.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpSize
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.android254.presentation.common.adaptive.rememberContentPaneDirective
import kotlinx.collections.immutable.ImmutableList

@Composable
fun Navigation(
    navController: NavigationController,
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
    onActionClicked: () -> Unit = {},
    supportingRoute: NavKey? = null,
    onBack: () -> Unit = { navController.goBack() },
    sceneStrategies: ImmutableList<SceneStrategy<NavKey>>? = null,
    entryProvider: (NavKey) -> NavEntry<NavKey> =
        droidconEntryProvider(
            navController,
            onActionClicked,
        ),
) {
    val transitionSpec =
        when (navigationState.lastDirection) {
            NavDirection.LEFT -> horizontalSlideIn(reverse = false)
            NavDirection.RIGHT -> horizontalSlideIn(reverse = true)
            NavDirection.INNER -> zoomInTransition()
        }
    val backTransitionSpec =
        when (navigationState.lastDirection) {
            NavDirection.LEFT, NavDirection.RIGHT -> horizontalSlideIn(reverse = true)
            else -> zoomOutTransition()
        }

    // Measured here rather than taken from the window: the navigation component is outside this
    // box but inside the window, so a window-sized directive splits space a drawer already has.
    BoxWithConstraints(modifier = modifier) {
        val directive = rememberContentPaneDirective(DpSize(maxWidth, maxHeight))
        val strategies = sceneStrategies ?: rememberDroidconSceneStrategies(directive)

        // A supporting entry that will not fit beside the screen would become the screen, because
        // the display falls back to drawing the last entry it was given.
        val supporting = supportingRoute.takeIf { directive.maxHorizontalPartitions > 1 }
        SharedTransitionLayout {
            // Shared elements only animate when drilling to or from a detail (INNER); switching
            // top-level tabs (LEFT/RIGHT) must not fly cards that appear on both tabs.
            val sharedScope = if (navigationState.lastDirection == NavDirection.INNER) this else null
            CompositionLocalProvider(LocalSharedTransitionScope provides sharedScope) {
                NavDisplay(
                    modifier = Modifier.testTag("navigation_display"),
                    entries = navigationState.toEntries(entryProvider, supporting),
                    sceneStrategies = strategies,
                    transitionSpec = { transitionSpec },
                    popTransitionSpec = { backTransitionSpec },
                    predictivePopTransitionSpec = { backTransitionSpec },
                    onBack = onBack,
                )
            }
        }
    }
}