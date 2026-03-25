package com.android254.presentation.common.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.*
import androidx.compose.animation.core.tween

const val DEFAULT_DURATION = 700

fun zoomInTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(durationMillis = DEFAULT_DURATION)
        ) + fadeIn(animationSpec = tween(DEFAULT_DURATION)),
        initialContentExit = scaleOut(
            targetScale = 1.2f,
            animationSpec = tween(durationMillis = DEFAULT_DURATION)
        ) + fadeOut(animationSpec = tween(DEFAULT_DURATION))
    )
}

fun zoomOutTransition(): ContentTransform {
    return ContentTransform(
        targetContentEnter = scaleIn(
            initialScale = 1.2f,
            animationSpec = tween(durationMillis = DEFAULT_DURATION)
        ) + fadeIn(animationSpec = tween(DEFAULT_DURATION)),
        initialContentExit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(durationMillis = DEFAULT_DURATION)
        ) + fadeOut(animationSpec = tween(DEFAULT_DURATION))

    )
}

fun horizontalSlideIn(reverse: Boolean = false): ContentTransform = slideInHorizontally(
    initialOffsetX = { if (reverse) -it else it },
    animationSpec = tween(DEFAULT_DURATION)
) + fadeIn(animationSpec = tween(DEFAULT_DURATION)) togetherWith slideOutHorizontally(
    targetOffsetX = { if (reverse) it else -it },
    animationSpec = tween(DEFAULT_DURATION)
) + fadeOut(animationSpec = tween(DEFAULT_DURATION))