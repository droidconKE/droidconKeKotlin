package com.android254.presentation.common.stepper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class VerticalStep<T>(
    val id: Any,
    val color: Color,
    val icon: ImageVector,
    val data: T
)
