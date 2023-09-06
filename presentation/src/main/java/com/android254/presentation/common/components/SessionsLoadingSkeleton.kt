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

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ke.droidcon.kotlin.presentation.R

@Composable
fun SessionsLoadingSkeleton() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = FastOutSlowInEasing

            )
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )

    for (i in 0 until 10) {
        LoadingGridItem(brush)
        if (i != 2) {
            Box(
                Modifier.padding(
                    start = 40.dp,
                    end = 0.dp,
                    top = 10.dp,
                    bottom = 10.dp
                )
            ) {
                Image(
                    painter = painterResource(id = if (i % 2 == 0) R.drawable.ic_green_session_card_spacer else R.drawable.ic_orange_session_card_spacer),
                    contentDescription = "spacer icon"
                )
            }
        }
    }
}

@Composable
fun LoadingGridItem(brush: Brush) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(5),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        onClick = {}
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(PaddingValues(top = 20.dp))
        ) {
            Column(
                modifier = Modifier
                    .weight(0.15f),
                horizontalAlignment = Alignment.End
            ) {
                LoaderCardElement(width = 40.dp, height = 20.dp, brush)
                Spacer(modifier = Modifier.height(8.dp))
                LoaderCardElement(width = 25.dp, height = 20.dp, brush)
            }

            Column(
                modifier = Modifier
                    .weight(0.85f)
                    .padding(PaddingValues(start = 10.dp, end = 10.dp, bottom = 10.dp))
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LoaderCardElement(width = 200.dp, height = 20.dp, brush)
                    Spacer(modifier = Modifier.width(20.dp))
                    LoaderCardElement(width = 20.dp, height = 20.dp, brush)
                }
                Column() {
                    for (i in 1 until 5) {
                        LoaderCardElement(width = (250 - i * 10).dp, height = 20.dp, brush)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LoaderCardElement(width = 120.dp, height = 20.dp, brush)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun LoaderCardElement(width: Dp, height: Dp, brush: Brush) {
    Spacer(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .width(width)
            .height(height)
            .background(brush)

    )
}