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
package com.droidconke.chai.hero

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.lifecycle.compose.LifecycleResumeEffect

/** The conference cube on its AI face, for AI features: it twists and solves while [thinking], and sways gently otherwise. */
@Composable
fun AiCube(
    thinking: Boolean,
    modifier: Modifier = Modifier,
) {
    val motion = remember { CubeMotion(mutableIntStateOf(AiScene)) }
    var resumed by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        resumed = true
        onPauseOrDispose { resumed = false }
    }
    LaunchedEffect(Unit) { CubeMarks.cutAll() }
    LaunchedEffect(thinking, resumed, visible) {
        if (resumed && visible) {
            if (thinking) motion.think() else motion.sway()
        }
    }
    CubeCanvas(motion, modifier.onVisibilityChanged(minFractionVisible = VISIBLE_FRACTION) { visible = it })
}

private val AiScene = Showcase.indexOf(CubeFace.Down)