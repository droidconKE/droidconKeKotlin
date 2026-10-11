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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.adaptive.rememberShowsAppBarLogo
import com.android254.presentation.common.insets.DroidconWindowInsets
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.components.ChaiBodyLarge
import com.droidconke.chai.isDarkTheme
import ke.droidcon.kotlin.core.ui.R

@Composable
fun DroidconAppBarWithFilter(
    isListActive: Boolean,
    onListIconClick: () -> Unit,
    onAgendaIconClick: () -> Unit,
    isFilterActive: Boolean,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = DroidconWindowInsets.appBar,
    showLogo: Boolean = rememberShowsAppBarLogo(),
    onFilterButtonClick: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .heightIn(min = 64.dp)
                .padding(horizontal = 20.dp)
                .testTag("droidcon_topBar_with_Filter"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showLogo) {
            Image(
                painter = painterResource(id = if (MaterialTheme.isDarkTheme) R.drawable.droidcon_logo_dark else R.drawable.droidcon_logo),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier.weight(1f, fill = false),
            )
        } else {
            Spacer(modifier = Modifier)
        }

        LayoutIconButtons(
            isListActive = isListActive,
            onListIconClick = onListIconClick,
            onAgendaIconClick = onAgendaIconClick,
        )

        FilterButton(
            isActive = isFilterActive,
            onButtonClick = onFilterButtonClick,
        )
    }
}

@Composable
fun LayoutIconButtons(
    isListActive: Boolean,
    onListIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAgendaIconClick: () -> Unit,
) {
    val listIconColor = if (isListActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val agendaIconColor = if (!isListActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LayoutIcon(
            selected = isListActive,
            onClick = onListIconClick,
            painter = painterResource(id = R.drawable.ic_listalt),
            contentDescription = stringResource(id = R.string.list_layout),
            tint = listIconColor,
        )

        Spacer(modifier = Modifier.width(11.dp))

        LayoutIcon(
            selected = !isListActive,
            onClick = onAgendaIconClick,
            painter = painterResource(id = R.drawable.ic_view_agenda),
            contentDescription = stringResource(id = R.string.agenda_layout),
            tint = agendaIconColor,
        )
    }
}

@Composable
private fun LayoutIcon(
    selected: Boolean,
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
    tint: Color,
) {
    Box(
        modifier =
            Modifier
                .minimumInteractiveComponentSize()
                .selectable(
                    selected = selected,
                    interactionSource = null,
                    indication = ripple(bounded = false, radius = 24.dp),
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painter, contentDescription = contentDescription, tint = tint)
    }
}

@Composable
fun FilterButton(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit,
) {
    val stateColors = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val activeState = stringResource(id = R.string.filter_applied)

    Row(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .clickable(role = Role.Button, onClick = onButtonClick)
                .semantics { if (isActive) stateDescription = activeState },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChaiBodyLarge(
            modifier = Modifier,
            bodyText = stringResource(id = R.string.top_bar_filter),
            textColor = stateColors,
        )
        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_filter),
            contentDescription = null,
            tint = stateColors,
        )
    }
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun ToolbarPreview() {
    ChaiTheme {
        Column {
            DroidconAppBarWithFilter(
                isListActive = true,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = true,
                onFilterButtonClick = {},
            )
            Spacer(
                modifier =
                    Modifier
                        .height(32.dp)
                        .background(color = MaterialTheme.colorScheme.outlineVariant),
            )

            DroidconAppBarWithFilter(
                isListActive = true,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = false,
                onFilterButtonClick = {},
            )
            Spacer(
                modifier =
                    Modifier
                        .height(32.dp)
                        .background(color = MaterialTheme.colorScheme.outlineVariant),
            )

            DroidconAppBarWithFilter(
                isListActive = false,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = true,
                onFilterButtonClick = {},
            )
            Spacer(
                modifier =
                    Modifier
                        .height(32.dp)
                        .background(color = MaterialTheme.colorScheme.outlineVariant),
            )

            DroidconAppBarWithFilter(
                isListActive = false,
                onListIconClick = {},
                onAgendaIconClick = {},
                isFilterActive = false,
                onFilterButtonClick = {},
            )
        }
    }
}