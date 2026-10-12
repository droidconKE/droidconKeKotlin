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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.droidconke.chai.chaiColorsPalette
import kotlin.math.roundToInt

/**
 * The conference hero card, as on the conference posters: [content] leading and the cube tumbling
 * in the bottom-end corner, bleeding off the card. On narrow cards the content takes the full width
 * and the cube sits below it; on wider ones they share the card side by side. The cube never sits
 * under the content, and at large font sizes it steps aside.
 */
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
        val stacked = maxWidth < SideBySideMinWidth
        val cubeSize = if (stacked) min(maxWidth * STACKED_CUBE_SHARE, StackedCubeMaxSize) else SideCubeSize
        if (cube) {
            CubeHero(
                animate = animateCube,
                modifier = Modifier.matchParentSize().inCorner(cubeSize, bleed = if (stacked) STACKED_BLEED else SIDE_BLEED).testTag("cube_hero"),
            )
        }
        Column(
            modifier =
                Modifier
                    .then(if (stacked || !cube) Modifier.fillMaxWidth() else Modifier.fillMaxWidth(SIDE_CONTENT_FRACTION))
                    .heightIn(min = if (cube && !stacked) SideCubeSize * SIDE_ROOM else 0.dp)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        ) {
            content()
            if (cube && stacked) Spacer(Modifier.height(cubeSize * STACKED_ROOM))
        }
    }
}

/**
 * Centres the cube, [size] across, in the bottom-end corner with [bleed] of it past the card's
 * edges. CubeHero draws the cube across about half its canvas, so the canvas is twice [size].
 */
private fun Modifier.inCorner(
    size: Dp,
    bleed: Float,
) = layout { measurable, constraints ->
    val cube = size.toPx()
    val canvas = (cube * 2).roundToInt()
    val placeable = measurable.measure(Constraints.fixed(canvas, canvas))
    val centreX = constraints.maxWidth - cube * (HALF - bleed)
    val centreY = constraints.maxHeight - cube * (HALF - bleed)
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.placeRelative((centreX - canvas / 2f).roundToInt(), (centreY - canvas / 2f).roundToInt())
    }
}

private val SideBySideMinWidth = 480.dp
private val SideCubeSize = 190.dp
private val StackedCubeMaxSize = 220.dp
private const val STACKED_CUBE_SHARE = 0.55f
private const val STACKED_ROOM = 0.9f
private const val STACKED_BLEED = 0.1f
private const val SIDE_BLEED = 0.05f
private const val SIDE_ROOM = 1.25f
private const val SIDE_CONTENT_FRACTION = 0.6f
private const val HALF = 0.5f
private const val LARGE_FONT_SCALE = 1.5f