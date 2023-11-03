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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.theme.Montserrat
import com.android254.presentation.models.SessionsFilterOption
import com.droidconke.chai.atoms.MontserratBold

@Composable
fun MultiToggleButton(
    currentSelections: List<SessionsFilterOption>,
    toggleStates: List<SessionsFilterOption>,
    modifier: Modifier = Modifier,
    borderSize: Dp = 1.dp,
    buttonHeight: Dp = 40.dp,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = selectedColor,
    border: BorderStroke = BorderStroke(borderSize, selectedColor),
    enabled: Boolean = true,
    onClick: (SessionsFilterOption) -> Unit
) {
    val unselectedColor = Color.Unspecified
    val selectedContentColor = MaterialTheme.colorScheme.onPrimary
    val unselectedContentColor = MaterialTheme.colorScheme.primary
    val chunkedOptions = toggleStates.chunked(3)

    chunkedOptions.forEach { filterOptions ->
        Row(modifier = modifier) {
            val squareCorner = CornerSize(0.dp)
            val buttonCount = filterOptions.size
            val shape = MaterialTheme.shapes.small

            filterOptions.forEachIndexed { index, sessionsFilterOption ->
                val buttonShape = when (index) {
                    0 -> shape.copy(bottomEnd = squareCorner, topEnd = squareCorner)
                    buttonCount - 1 -> shape.copy(
                        topStart = squareCorner,
                        bottomStart = squareCorner
                    )
                    else -> shape.copy(all = squareCorner)
                }
                val isButtonSelected = currentSelections.contains(sessionsFilterOption)
                val backgroundColor = if (isButtonSelected) selectedColor else unselectedColor
                val contentColor =
                    if (isButtonSelected) selectedContentColor else unselectedContentColor
                val textStyle =
                    if (isButtonSelected) {
                        TextStyle(fontFamily = MontserratBold)
                    } else {
                        TextStyle(
                            fontFamily = Montserrat
                        )
                    }
                val offset = borderSize * -index

                ToggleButton(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .defaultMinSize(minHeight = buttonHeight)
                        .offset(x = offset),
                    buttonShape = buttonShape,
                    border = border,
                    backgroundColor = backgroundColor,
                    elevation = ButtonDefaults.buttonElevation(),
                    enabled = enabled,
                    buttonTexts = filterOptions.map {
                        it.label
                    },
                    index = index,
                    contentColor = contentColor,
                    textStyle = textStyle,
                    onClick = {
                        onClick(sessionsFilterOption)
                    }
                )
            }
        }
    }
}

@Composable
private fun ToggleButton(
    modifier: Modifier,
    buttonShape: CornerBasedShape,
    border: BorderStroke,
    backgroundColor: Color,
    elevation: ButtonElevation,
    enabled: Boolean,
    buttonTexts: List<String>,
    index: Int,
    contentColor: Color,
    onClick: () -> Unit,
    textStyle: TextStyle
) {
    OutlinedButton(
        modifier = modifier,
        contentPadding = PaddingValues(),
        shape = buttonShape,
        border = border,
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor),
        elevation = elevation,
        enabled = enabled
    ) {
        ButtonContent(
            buttonTexts = buttonTexts,
            index = index,
            textStyle = textStyle,
            contentColor = contentColor
        )
    }
}

@Composable
private fun RowScope.ButtonContent(
    buttonTexts: List<String>,
    index: Int,
    contentColor: Color,
    textStyle: TextStyle
) {
    when {
        buttonTexts.all { it != "" } -> TextContent(
            modifier = Modifier.align(Alignment.CenterVertically),
            buttonTexts = buttonTexts,
            index = index,
            contentColor = contentColor,
            textStyle = textStyle
        )
    }
}

@Composable
private fun TextContent(
    modifier: Modifier,
    buttonTexts: List<String>,
    index: Int,
    contentColor: Color,
    textStyle: TextStyle
) {
    Text(
        modifier = modifier.padding(horizontal = 8.dp),
        text = buttonTexts[index],
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = textStyle
    )
}