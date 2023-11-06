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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.MultiToggleButton
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import com.droidconke.chai.atoms.ChaiGrey90
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.CButton
import com.droidconke.chai.components.CPrimaryButtonText
import com.droidconke.chai.components.ChaiBodyLarge
import com.droidconke.chai.components.ChaiSubTitle
import com.droidconke.chai.components.ChaiTextButtonLight
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
    val context = LocalContext.current

    val selectableFilters = loadFilters(context)

    val groupedFilters = selectableFilters.groupBy {
        it.type
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = ChaiGrey90.copy(alpha = 0.52f)
            )
    ) {
        // The Visible Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(
                    color = MaterialTheme.chaiColorsPalette.bottomSheetBackgroundColor,
                    shape = RoundedCornerShape(bottomEnd = 14.dp, bottomStart = 14.dp)
                )
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 36.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 12.dp),
                        tint = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor
                    )
                    ChaiBodyLarge(
                        bodyText = stringResource(id = R.string.filter_button_label),
                        textColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor
                    )
                }

                ChaiTextButtonLight(
                    modifier = Modifier.clickable {
                        clearSelectedFilterList()
                        onDismiss()
                    },
                    bodyText = stringResource(id = R.string.cancel),
                    textColor = MaterialTheme.chaiColorsPalette.textWeakColor
                )
            }

            groupedFilters.forEach { filter ->
                Column(
                    Modifier
                        .fillMaxWidth()
                ) {
                    ChaiSubTitle(
                        titleText = filter.key.name,
                        titleColor = MaterialTheme.chaiColorsPalette.textNormalColor
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
                    containerColor = MaterialTheme.chaiColorsPalette.secondaryButtonColor,
                    contentColor = MaterialTheme.chaiColorsPalette.secondaryButtonTextColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                CPrimaryButtonText(
                    text = stringResource(R.string.filter_button_label).uppercase(),
                    textColor = MaterialTheme.chaiColorsPalette.secondaryButtonTextColor
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable {
                    onDismiss()
                }
        )
    }
}