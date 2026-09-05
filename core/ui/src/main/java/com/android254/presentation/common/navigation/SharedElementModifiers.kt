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
package com.android254.presentation.common.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.navigation3.ui.LocalNavAnimatedContentScope

// Null unless a composable is hosted by the SharedTransitionLayout that wraps NavDisplay
// (so a preview, a test, or a ComposeView outside navigation makes the modifiers below no-op
// instead of crashing on the LocalNavAnimatedContentScope read). Passing the scope this way
// mirrors how Nav3 delivers LocalNavAnimatedContentScope, rather than threading it through
// every screen signature.
@Suppress("ComposeCompositionLocalUsage")
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope?> =
    compositionLocalOf { null }

@Composable
fun Modifier.sessionSharedImage(
    sessionId: String,
    shape: Shape,
): Modifier = sharedImage(key = "session-image-$sessionId", shape = shape)

@Composable
fun Modifier.sessionSharedTitle(sessionId: String): Modifier = sharedText(key = "session-title-$sessionId")

@Composable
fun Modifier.speakerSharedImage(
    speakerName: String,
    shape: Shape,
): Modifier = sharedImage(key = "speaker-image-$speakerName", shape = shape)

@Composable
fun Modifier.speakerSharedName(speakerName: String): Modifier = sharedText(key = "speaker-name-$speakerName")

@Composable
private fun Modifier.sharedImage(
    key: String,
    shape: Shape,
): Modifier = sharedBoundsIfHosted(key = key, shape = shape).clip(shape)

@Composable
private fun Modifier.sharedBoundsIfHosted(
    key: String,
    shape: Shape,
): Modifier {
    if (LocalInspectionMode.current) return this
    val scope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalNavAnimatedContentScope.current
    return with(scope) {
        this@sharedBoundsIfHosted.sharedBounds(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            clipInOverlayDuringTransition = OverlayClip(shape),
        )
    }
}

@Composable
private fun Modifier.sharedText(key: String): Modifier {
    if (LocalInspectionMode.current) return this
    val scope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalNavAnimatedContentScope.current
    return with(scope) {
        this@sharedText.sharedBounds(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
        )
    }
}