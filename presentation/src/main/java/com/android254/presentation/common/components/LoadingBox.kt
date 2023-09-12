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
){
    Box(
        modifier = Modifier
            .customWidth(widthRatio, width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .customBackground(brush, color)
    )
}


fun Modifier.customBackground(brush: Brush?, color: Color) = if (brush != null) background(brush)
    else background(color)

fun Modifier.customWidth(ratio: Float?, width: Dp) = if (ratio!=null) fillMaxWidth(ratio)
    else width(width)