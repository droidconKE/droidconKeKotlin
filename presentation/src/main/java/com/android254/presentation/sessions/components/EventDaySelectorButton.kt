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
package com.android254.presentation.sessions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiSubTitle
import com.droidconke.chai.components.ChaiTextLabelLarge

@Composable
fun EventDaySelectorButton(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .size(51.dp)
            .clickable { onClick() }
            .background(
                color = if (selected) MaterialTheme.chaiColorsPalette.eventDaySelectorActiveSurfaceColor else (MaterialTheme.chaiColorsPalette.eventDaySelectorInactiveSurfaceColor).copy(alpha = 0.11f),
                shape = RoundedCornerShape(5.dp)
            )
            .padding(start = 5.dp)
    ) {
        ChaiSubTitle(
            titleText = title,
            titleColor = if (selected) MaterialTheme.chaiColorsPalette.eventDaySelectorActiveTextColor else MaterialTheme.chaiColorsPalette.eventDaySelectorInactiveTextColor
        )
        ChaiTextLabelLarge(
            bodyText = subtitle,
            textColor = if (selected) MaterialTheme.chaiColorsPalette.eventDaySelectorActiveTextColor else MaterialTheme.chaiColorsPalette.eventDaySelectorInactiveTextColor
        )
    }
}

@ChaiLightAndDarkComposePreview
@Composable
private fun EventDaySelectorButtonPreview() {
    ChaiDCKE22Theme {
        Row {
            EventDaySelectorButton(
                title = "10th",
                subtitle = "Day 1",
                selected = true,
                onClick = {}
            )
            EventDaySelectorButton(
                title = "10th",
                subtitle = "Day 1",
                selected = false,
                onClick = {}
            )
        }
    }
}