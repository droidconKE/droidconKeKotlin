/*
 * Copyright 2023 DroidconKE
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingBox(
    height: Dp,
    width: Dp = 0.dp,
    widthRatio: Float? = null,
    cornerRadius: Dp = 5.dp,
    brush: Brush? = null,
    color: Color = Color.LightGray.copy(alpha = 0.5f)
) {
    Box(
        modifier = Modifier
            .customWidth(widthRatio, width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .customBackground(brush, color)
    )
}

fun Modifier.customBackground(brush: Brush?, color: Color) = if (brush != null) {
    background(brush)
} else {
    background(color)
}

fun Modifier.customWidth(ratio: Float?, width: Dp) = if (ratio != null) {
    fillMaxWidth(ratio)
} else {
    width(width)
}