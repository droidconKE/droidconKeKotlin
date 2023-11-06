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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.*
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLarge
import ke.droidcon.kotlin.presentation.R

@Composable
fun DroidconAppBarWithFilter(
    modifier: Modifier = Modifier,
    isListActive: Boolean,
    onListIconClick: () -> Unit,
    onAgendaIconClick: () -> Unit,
    isFilterActive: Boolean,
    onFilterButtonClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 20.dp, end = 20.dp, top = 19.dp, bottom = 15.dp)
            .testTag("droidcon_topBar_with_Filter"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = if (isSystemInDarkTheme()) R.drawable.droidcon_logo_dark else R.drawable.droidcon_logo),
            contentDescription = stringResource(id = R.string.logo)
        )
        Spacer(modifier = Modifier.weight(1f))

        LayoutIconButtons(
            isListActive = isListActive,
            onListIconClick = onListIconClick,
            onAgendaIconClick = onAgendaIconClick
        )
        Spacer(modifier = Modifier.weight(1f))

        FilterButton(
            isActive = isFilterActive,
            onButtonClick = onFilterButtonClick
        )
    }
}

@Composable
fun LayoutIconButtons(
    modifier: Modifier = Modifier,
    isListActive: Boolean,
    onListIconClick: () -> Unit,
    onAgendaIconClick: () -> Unit
) {
    val listIconColor = if (isListActive) MaterialTheme.chaiColorsPalette.secondaryButtonColor else MaterialTheme.chaiColorsPalette.radioButtonColors
    val agendaIconColor = if (!isListActive) MaterialTheme.chaiColorsPalette.secondaryButtonColor else MaterialTheme.chaiColorsPalette.radioButtonColors

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clickable(
                    onClick = onListIconClick
                ),
            painter = painterResource(id = R.drawable.ic_listalt),
            contentDescription = null,
            tint = listIconColor
        )

        Spacer(modifier = Modifier.width(35.dp))

        Icon(
            modifier = Modifier
                .clickable(
                    onClick = onAgendaIconClick
                ),
            painter = painterResource(id = R.drawable.ic_view_agenda),
            contentDescription = null,
            tint = agendaIconColor
        )
    }
}

@Composable
fun FilterButton(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onButtonClick: () -> Unit
) {
    val stateColors = if (isActive) MaterialTheme.chaiColorsPalette.secondaryButtonColor else ChaiGrey

    Row(
        modifier = modifier
            .clickable(
                enabled = isActive,
                onClick = onButtonClick
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChaiBodyLarge(
            modifier = Modifier,
            bodyText = stringResource(id = R.string.top_bar_filter),
            textColor = stateColors
        )
        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_filter),
            contentDescription = null,
            tint = stateColors
        )
    }
}

@ChaiLightAndDarkComposePreview
@Composable
fun ToolbarPreview() {
    ChaiDCKE22Theme {
        Column() {
            DroidconAppBarWithFilter(
                isListActive = true,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = true,
                onFilterButtonClick = {}
            )
            Spacer(
                modifier = Modifier
                    .height(32.dp)
                    .background(color = ChaiGrey)
            )

            DroidconAppBarWithFilter(
                isListActive = true,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = false,
                onFilterButtonClick = {}
            )
            Spacer(
                modifier = Modifier
                    .height(32.dp)
                    .background(color = ChaiGrey)
            )

            DroidconAppBarWithFilter(
                isListActive = false,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = true,
                onFilterButtonClick = {}
            )
            Spacer(
                modifier = Modifier
                    .height(32.dp)
                    .background(color = ChaiGrey)
            )

            DroidconAppBarWithFilter(
                isListActive = false,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = false,
                onFilterButtonClick = {}
            )
        }
    }
}