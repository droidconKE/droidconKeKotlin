package com.android254.presentation.common.stepper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    nextStepColor: Color? = null,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    bottomSpacing: Dp = 0.dp,
    content: @Composable (T) -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val currentColor = step.color ?: primary
    val nextColor = nextStepColor ?: (if (isLast) currentColor else primary)

    val connectorAlpha = 0.4f
    val currentConnectorColor = currentColor.copy(alpha = connectorAlpha)
    val nextConnectorColor = nextColor.copy(alpha = connectorAlpha)

    // Gradient that transitions the line from this step's color to the next
    val lineBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to currentConnectorColor,
            0.6f to currentConnectorColor,
            0.85f to nextConnectorColor,
            1.0f to nextConnectorColor,
        ),
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val topWeight = when {
                    isFirst -> 0.2f
                    isLast -> 0.8f
                    else -> 0.5f
                }

                val bottomWeight = 1f - topWeight

                // Top connector: Solid color of CURRENT step (matches previous bridge)
                Box(
                    modifier = Modifier
                        .weight(topWeight)
                        .width(2.dp)
                        .then(
                            if (isFirst) Modifier else Modifier.background(currentConnectorColor),
                        ),
                )

                Spacer(modifier = Modifier.height(4.dp))

                IconBox(
                    icon = step.icon ?: Icons.Outlined.Circle,
                    accentColor = currentColor,
                    sizeInt = 30,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom connector: Gradient from CURRENT to NEXT
                Box(
                    modifier = Modifier
                        .weight(bottomWeight)
                        .width(2.dp)
                        .then(
                            if (isLast) Modifier else Modifier.background(lineBrush),
                        ),
                )
            }

            Spacer(Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                content(step.data)
            }
        }

        // Bridge the gap for spacing between items
        if (!isLast && bottomSpacing > 0.dp) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(bottomSpacing),
                contentAlignment = Alignment.Center,
            ) {
                // The bridge uses the NEXT color solid, as the gradient above already finished
                Box(
                    modifier = Modifier
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
) {
    Box(
        modifier = modifier
            .size(sizeInt.dp)
            .clip(CircleShape)
            .background(accentColor.copy(0.2f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size((sizeInt / 2).dp),
        )
    }
}