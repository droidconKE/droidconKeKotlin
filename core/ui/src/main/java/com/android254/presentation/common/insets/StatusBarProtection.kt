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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag

/** Test tag, so a screen that must draw its own protection can be asserted to. */
const val STATUS_BAR_PROTECTION_TEST_TAG: String = "status_bar_protection"

/**
 * A scrim over the status bar, drawn on top of content that scrolls underneath it.
 *
 * Only a screen with no app bar needs this: an app bar is an opaque surface and the clock
 * already reads against it. A detail pane, which drops its app bar because the list beside it
 * already says where you are, has nothing up there but its own scrolling body — and a session
 * title passing under the clock is what makes the clock illegible.
 *
 * Taller than the status bar by design (the skill's 1.2 factor), so the gradient has somewhere
 * to fade out instead of ending in a visible edge.
 */
@Composable
fun StatusBarProtection(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    val height = with(LocalDensity.current) { (WindowInsets.statusBars.getTop(this) * 1.2f).toDp() }
    Spacer(
        modifier =
            modifier
                .testTag(STATUS_BAR_PROTECTION_TEST_TAG)
                .fillMaxWidth()
                .height(height)
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    color.copy(alpha = 1f),
                                    color.copy(alpha = 0.8f),
                                    Color.Transparent,
                                ),
                        ),
                ),
    )
}