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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.droidconke.chai.colors.CubeStageColor
import com.droidconke.chai.colors.CubeStageGlowColor
import kotlin.math.roundToInt

/**
 * The cube's stage, as on the conference posters: the cube turning on a dark card with [content]
 * centred below it. The cube steps aside at large font sizes.
 */
@Composable
fun CubeHeroStage(
    modifier: Modifier = Modifier,
    showCube: Boolean = true,
    animateCube: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cube = showCube && LocalDensity.current.fontScale <= LARGE_FONT_SCALE
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(CubeStageColor),
    ) {
        val cubeSize = min(maxWidth * STAGE_CUBE_SHARE, MaxStageCubeSize)
        if (cube) {
            Box(
                Modifier
                    .matchParentSize()
                    .drawBehind {
                        val centre = Offset(size.width / 2, StageTopPadding.toPx() + cubeSize.toPx() / 2)
                        val radius = cubeSize.toPx() * GLOW_REACH
                        drawCircle(Brush.radialGradient(listOf(CubeStageGlowColor.copy(alpha = GLOW_ALPHA), Color.Transparent), centre, radius), radius, centre)
                    },
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = StageTopPadding, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (cube) {
                CubeHero(
                    animate = animateCube,
                    modifier = Modifier.size(cubeSize).centred(cubeSize).testTag("cube_hero"),
                )
                Spacer(Modifier.height(CubeGap))
            }
            content()
        }
    }
}

/** Takes [size] in the layout and draws the cube that size, on the larger canvas it needs. */
private fun Modifier.centred(size: Dp) =
    layout { measurable, _ ->
        val slot = size.roundToPx()
        val canvas = (slot * CANVAS_PER_CUBE).roundToInt()
        val placeable = measurable.measure(Constraints.fixed(canvas, canvas))
        layout(slot, slot) { placeable.placeRelative((slot - canvas) / 2, (slot - canvas) / 2) }
    }

private val MaxStageCubeSize = 240.dp
private val StageTopPadding = 24.dp
private val CubeGap = 12.dp
private const val STAGE_CUBE_SHARE = 0.6f
private const val GLOW_REACH = 0.9f
private const val GLOW_ALPHA = 0.45f