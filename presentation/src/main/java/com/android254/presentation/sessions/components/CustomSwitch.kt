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
package com.android254.presentation.sessions.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiTextLabelSmall

@Composable
fun CustomSwitch(
    width: Dp = 56.dp,
    height: Dp = 32.dp,
    iconInnerPadding: Dp = 4.dp,
    thumbSize: Dp = 24.dp,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // this is to disable the ripple effect
    val interactionSource = remember {
        MutableInteractionSource()
    }

    // for moving the thumb
    val alignment by animateAlignmentAsState(if (checked) 1f else -1f)

    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // outer toggle container
        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .clickable(
                    indication = null,
                    interactionSource = interactionSource
                ) {
                    onCheckedChange(!checked)
                },
            contentAlignment = Alignment.Center
        ) {
            // this is the horizontal rounded rectangle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = if (checked) MaterialTheme.chaiColorsPalette.toggleOnBackgroundColor else MaterialTheme.chaiColorsPalette.toggleOffBackgroundColor
                    )
                    .height(17.dp)
                    .width(54.dp)
            )
            // thumb with icon
            Icon(
                modifier = Modifier
                    .size(size = thumbSize)
                    .background(
                        color = if (checked) MaterialTheme.chaiColorsPalette.toggleOnIconBackgroundColor else MaterialTheme.chaiColorsPalette.toggleOffIconBackgroundColor,
                        shape = CircleShape
                    )
                    .padding(all = iconInnerPadding)
                    .align(alignment),
                imageVector = Icons.Filled.Star,
                contentDescription = if (checked) "Enabled" else "Disabled",
                tint = if (checked) MaterialTheme.chaiColorsPalette.toggleOnIconColor else MaterialTheme.chaiColorsPalette.toggleOffIconColor
            )
        }

        ChaiTextLabelSmall(
            bodyText = "My sessions",
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor
        )
    }
}

@Composable
private fun animateAlignmentAsState(
    targetBiasValue: Float
): State<BiasAlignment> {
    val bias by animateFloatAsState(targetValue = targetBiasValue, label = "")

    return remember {
        derivedStateOf { BiasAlignment(horizontalBias = bias, verticalBias = 0f) }
    }
}

@ChaiLightAndDarkComposePreview
@Composable
private fun CustomSwitchPreview() {
    ChaiDCKE22Theme {
        Column(
            modifier = Modifier.background(color = MaterialTheme.chaiColorsPalette.background),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CustomSwitch(checked = false, onCheckedChange = {})
            CustomSwitch(checked = true, onCheckedChange = {})
        }
    }
}