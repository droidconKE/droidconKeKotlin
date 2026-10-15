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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.droidconke.chai.atoms.LARGE_FONT_SCALE
import com.droidconke.chai.chaiColorsPalette
import kotlin.math.roundToInt

/** The hero card: [content] at the start and the cube at the end, past the card's edge. The cube steps aside at large font sizes. */
@Composable
fun CubeHeroBanner(
    modifier: Modifier = Modifier,
    showCube: Boolean = true,
    animateCube: Boolean = true,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cube = showCube && LocalDensity.current.fontScale <= LARGE_FONT_SCALE
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.chaiColorsPalette.heroContainerColor)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
    ) {
        val cubeSize = (maxWidth * CUBE_SHARE).coerceIn(MinCubeSize, MaxCubeSize)
        if (cube) {
            CubeHero(
                animate = animateCube,
                modifier = Modifier.matchParentSize().atEnd(cubeSize),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (cube) cubeSize + CubeMargin * 2 else 0.dp)
                    .padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = if (cube) cubeSize * (1 - BLEED) + TextGap else 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        ) {
            content()
        }
    }
}

private fun Modifier.atEnd(size: Dp) =
    layout { measurable, constraints ->
        val cube = size.toPx()
        val canvas = (cube * CANVAS_PER_CUBE).roundToInt()
        val placeable = measurable.measure(Constraints.fixed(canvas, canvas))
        val centreX = constraints.maxWidth - cube * (HALF - BLEED)
        val centreY = constraints.maxHeight / 2f
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative((centreX - canvas / 2f).roundToInt(), (centreY - canvas / 2f).roundToInt())
        }
    }

private val MinCubeSize = 132.dp
private val MaxCubeSize = 206.dp
private val CubeMargin = 8.dp
private val TextGap = 8.dp
private const val CUBE_SHARE = 0.42f
private const val BLEED = 0.08f
private const val HALF = 0.5f