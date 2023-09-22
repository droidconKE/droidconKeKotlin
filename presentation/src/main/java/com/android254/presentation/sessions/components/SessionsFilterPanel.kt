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

import android.content.Context
import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android254.presentation.common.components.MultiToggleButton
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import com.droidconke.chai.atoms.MontserratBold
import com.droidconke.chai.atoms.MontserratSemiBold
import com.droidconke.chai.components.CButton
import ke.droidcon.kotlin.presentation.R

private fun loadFilters(context: Context): List<SessionsFilterOption> {
    val resources: Resources = context.resources
    return listOf(
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = resources.getString(R.string.session_filter_label_keynote),
            value = "keynote"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = resources.getString(R.string.session_filter_label_codelab),
            value = "codelab"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = resources.getString(R.string.session_filter_label_session),
            value = "Session"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = resources.getString(R.string.session_filter_label_workshop),
            value = "Workshop"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = resources.getString(R.string.session_filter_label_lightning_talk),
            value = "Lightning talk"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = resources.getString(R.string.session_filter_label_panel_discussion),
            value = "Panel discussion"
        ),
        SessionsFilterOption(
            label = resources.getString(R.string.session_filter_label_room_a),
            value = "Room A",
            type = SessionsFilterCategory.Room
        ),
        SessionsFilterOption(
            label = resources.getString(R.string.session_filter_label_room_b),
            value = "Room B",
            type = SessionsFilterCategory.Room
        ),
        SessionsFilterOption(
            label = resources.getString(R.string.session_filter_label_room_c),
            value = "Room C",
            type = SessionsFilterCategory.Room
        ),

        SessionsFilterOption(
            label = resources.getString(R.string.session_filter_label_beginner),
            value = "Introductory and overview",
            type = SessionsFilterCategory.Level
        ),
        SessionsFilterOption(
            label = resources.getString(R.string.session_filter_label_intermediate),
            value = "Intermediate",
            type = SessionsFilterCategory.Level
        ),
        SessionsFilterOption(
            label = resources.getString(R.string.session_filter_label_advanced),
            value = "Advanced",
            type = SessionsFilterCategory.Level
        )
    )
}

@Composable
fun SessionsFilterPanel(
    onDismiss: () -> Unit,
    currentSelections: List<SessionsFilterOption>,
    updateSelectedFilterOptionList: (SessionsFilterOption) -> Unit,
    fetchSessionWithFilter: () -> Unit,
    clearSelectedFilterList: () -> Unit
) {
    val filterTypeTextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = MontserratBold
    )

    val context = LocalContext.current

    val selectableFilters = loadFilters(context)

    val groupedFilters = selectableFilters.groupBy {
        it.type
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 144.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)

        ) {
            IconButton(onClick = {
                clearSelectedFilterList()
                onDismiss()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_close_24),
                    contentDescription = stringResource(R.string.close_icon_description)
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                clearSelectedFilterList()
            }) {
                Text(
                    text = "Clear Filters".uppercase(),
                    style = TextStyle(
                        fontFamily = MontserratSemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        groupedFilters.forEach { filter ->
            Column(
                Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = filter.key.name,
                    style = filterTypeTextStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                MultiToggleButton(
                    toggleStates = filter.value,
                    onClick = {
                        updateSelectedFilterOptionList(it)
                    },
                    currentSelections = currentSelections
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        CButton(
            onClick = {
                fetchSessionWithFilter()
                onDismiss()
            },
            isEnabled = true,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_button_label).uppercase(),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = MontserratSemiBold,
                    letterSpacing = 1.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}