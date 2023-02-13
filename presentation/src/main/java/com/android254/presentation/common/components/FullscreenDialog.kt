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
package com.android254.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal const val ANIMATION_TIME = 500L
private const val DIALOG_BUILD_TIME = 50L

@Composable
internal fun AnimatedModalBottomSheetTransition(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(ANIMATION_TIME.toInt()),
            initialOffsetY = { fullHeight -> fullHeight }
        ),
        exit = slideOutVertically(
            animationSpec = tween(ANIMATION_TIME.toInt()),
            targetOffsetY = { fullHeight -> fullHeight }
        ),
        content = content
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FullscreenDialog(
    onDismissRequest: () -> Unit,
    dismissOnBackPress: Boolean = true,
    content: @Composable (ModalTransitionDialogHelper) -> Unit
) {
    val onCloseSharedFlow: MutableSharedFlow<Unit> = remember { MutableSharedFlow() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val animateContentBackTrigger = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        launch {
            delay(DIALOG_BUILD_TIME)
            animateContentBackTrigger.value = true
        }
        launch {
            onCloseSharedFlow.asSharedFlow().collectLatest { startDismissWithExitAnimation(animateContentBackTrigger, onDismissRequest) }
        }
    }

    Dialog(
        onDismissRequest = { coroutineScope.launch { startDismissWithExitAnimation(animateContentBackTrigger, onDismissRequest) } },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = dismissOnBackPress, dismissOnClickOutside = false)
    ) {
        Box(Modifier.fillMaxSize()) { // Required in order to occupy the whole screen before the animation is triggered
            AnimatedModalBottomSheetTransition(visible = animateContentBackTrigger.value) {
                content(ModalTransitionDialogHelper(coroutineScope, onCloseSharedFlow))
            }
        }
    }
}

private suspend fun startDismissWithExitAnimation(
    animateContentBackTrigger: MutableState<Boolean>,
    onDismissRequest: () -> Unit
) {
    animateContentBackTrigger.value = false
    delay(ANIMATION_TIME)
    onDismissRequest()
}

class ModalTransitionDialogHelper(
    private val coroutineScope: CoroutineScope,
    private val onCloseFlow: MutableSharedFlow<Unit>
) {
    fun triggerAnimatedClose() {
        coroutineScope.launch {
            onCloseFlow.emit(Unit)
        }
    }
}