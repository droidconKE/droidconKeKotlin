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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

/**
 * Who pays for which system inset, now that the app is edge-to-edge.
 *
 * Splitting it this way is what lets a screen draw under the status bar at all: if the
 * composition root consumed the top inset, as it did before, every screen would start below
 * the status bar and the edge-to-edge opt-in would buy nothing.
 */
object DroidconWindowInsets {
    /**
     * The app bar's share: the top, so its background tints the status bar strip and its
     * content sits clear of it, plus the horizontal cutout in landscape.
     */
    val appBar: WindowInsets
        @Composable
        get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

    /**
     * A screen's share, for its `Scaffold`'s `contentWindowInsets`.
     *
     * The top is absent on purpose — the screen's own app bar consumed it. The bottom
     * resolves to zero while the bottom bar is visible, because the root consumes it there,
     * and to the navigation bar or the keyboard, whichever is taller, when it is not.
     */
    val screenContent: WindowInsets
        @Composable
        get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
}