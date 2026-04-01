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

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

const val DEFAULT_DURATION = 700

fun zoomInTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter =
            scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(durationMillis = DEFAULT_DURATION),
            ) + fadeIn(animationSpec = tween(DEFAULT_DURATION)),
        initialContentExit =
            scaleOut(
                targetScale = 1.2f,
                animationSpec = tween(durationMillis = DEFAULT_DURATION),
            ) + fadeOut(animationSpec = tween(DEFAULT_DURATION)),
    )
}

fun zoomOutTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter =
            scaleIn(
                initialScale = 1.2f,
                animationSpec = tween(durationMillis = DEFAULT_DURATION),
            ) + fadeIn(animationSpec = tween(DEFAULT_DURATION)),
        initialContentExit =
            scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(durationMillis = DEFAULT_DURATION),
            ) + fadeOut(animationSpec = tween(DEFAULT_DURATION)),
    )
}

fun horizontalSlideIn(reverse: Boolean = false): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { if (reverse) -it else it },
        animationSpec = tween(DEFAULT_DURATION),
    ) + fadeIn(animationSpec = tween(DEFAULT_DURATION)) togetherWith slideOutHorizontally(
        targetOffsetX = { if (reverse) it else -it },
        animationSpec = tween(DEFAULT_DURATION),
    ) + fadeOut(animationSpec = tween(DEFAULT_DURATION))