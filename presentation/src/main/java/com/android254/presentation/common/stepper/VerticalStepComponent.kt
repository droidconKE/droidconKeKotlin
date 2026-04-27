package com.android254.presentation.common.stepper

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun <T> LazyListScope.verticalSteps(
    items: List<VerticalStep<T>>,
    spacing: Dp = 0.dp,
    content: @Composable (T) -> Unit,
) {
    itemsIndexed(
        items = items,
        key = { _, item -> item.id },
    ) { index, step ->
        val nextStep = items.getOrNull(index + 1)
        VerticalStepItem(
            step = step,
            nextStepColor = nextStep?.color,
            isFirst = index == 0,
            isLast = index == items.lastIndex,
            bottomSpacing = if (index == items.lastIndex) 0.dp else spacing,
            content = content,
        )
    }
}

@Composable
fun <T> VerticalStepItem(
    step: VerticalStep<T>,
    modifier: Modifier = Modifier,
    nextStepColor: Color? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    bottomSpacing: Dp = 0.dp,
    content: @Composable (T) -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val currentColor =
        if (step.intensity == Intensity.Low) {
            MaterialTheme.colorScheme.outline
        } else {
            step.color ?: primary
        }
    val nextColor =
        if (step.intensity == Intensity.Low) {
            MaterialTheme.colorScheme.outline
        } else {
            nextStepColor ?: (if (isLast) currentColor else primary)
        }

    val connectorAlpha = 0.4f
    val currentConnectorColor = currentColor.copy(alpha = connectorAlpha)
    val nextConnectorColor = nextColor.copy(alpha = connectorAlpha)

    // Gradient that transitions the line from this step's color to the next
    val lineBrush =
        Brush.verticalGradient(
            colorStops =
                arrayOf(
                    0.0f to currentConnectorColor,
                    0.6f to currentConnectorColor,
                    0.85f to nextConnectorColor,
                    1.0f to nextConnectorColor,
                ),
        )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
        ) {
            Column(
                modifier =
                    Modifier
                        .width(36.dp)
                        .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val topWeight =
                    when {
                        isFirst -> 0.2f
                        isLast -> 0.8f
                        else -> 0.5f
                    }

                val bottomWeight = 1f - topWeight

                // Top connector: Solid color of CURRENT step (matches previous bridge)
                Box(
                    modifier =
                        Modifier
                            .weight(topWeight)
                            .clip(CircleShape)
                            .width(2.dp)
                            .then(
                                if (isFirst) Modifier else Modifier.background(currentConnectorColor),
                            ),
                )

                Spacer(modifier = Modifier.height(4.dp))

                IconBox(
                    icon = step.icon,
                    accentColor = currentColor,
                    sizeInt = 48,
                    intensity = step.intensity,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom connector: Gradient from CURRENT to NEXT
                Box(
                    modifier =
                        Modifier
                            .weight(bottomWeight)
                            .width(2.dp)
                            .clip(CircleShape)
                            .then(
                                if (isLast) Modifier else Modifier.background(lineBrush),
                            ),
                )
            }

            Spacer(Modifier.width(16.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
            ) {
                content(step.data)
            }
        }

        // Bridge the gap for spacing between items
        if (!isLast && bottomSpacing > 0.dp) {
            Box(
                modifier =
                    Modifier
                        .width(36.dp)
                        .height(bottomSpacing),
                contentAlignment = Alignment.Center,
            ) {
                // The bridge uses the NEXT color solid, as the gradient above already finished
                Box(
                    modifier =
                        Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(nextConnectorColor),
                )
            }
        }
    }
}

@Composable
private fun IconBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    accentColor: Color,
    sizeInt: Int,
    intensity: Intensity = Intensity.Medium,
) {
    val colorScheme = MaterialTheme.colorScheme

    val backgroundColor =
        when (intensity) {
            Intensity.High -> accentColor
            Intensity.Medium -> Color.Transparent
            Intensity.Low -> Color.Transparent
        }

    val borderColor =
        when (intensity) {
            Intensity.High -> Color.Transparent
            Intensity.Medium -> accentColor
            Intensity.Low -> colorScheme.outline
        }

    val iconTint =
        when (intensity) {
            Intensity.High -> Color.White
            Intensity.Medium -> accentColor
            Intensity.Low -> colorScheme.outline
        }

    val iconAlpha =
        when (intensity) {
            Intensity.High -> 1f
            Intensity.Medium -> 1f
            Intensity.Low -> 0.6f
        }

    val shouldGlow = intensity == Intensity.High

    Box(
        modifier =
            modifier
                .size(sizeInt.dp)
                .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        // glow
        if (shouldGlow) {
            val transition = rememberInfiniteTransition(label = "ping")

            val scale by transition.animateFloat(
                initialValue = 1f,
                targetValue = 1.8f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(1400),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "scale",
            )

            val alpha by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(1400),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "alpha",
            )

            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .clip(CircleShape)
                        .background(accentColor),
            )
        }

        // core circle
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(
                        width = 1.5.dp,
                        color = borderColor,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint.copy(alpha = iconAlpha),
                modifier = Modifier.size((sizeInt / 2).dp),
            )
        }
    }
}