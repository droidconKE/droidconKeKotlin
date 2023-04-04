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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android254.droidconKE2023.presentation.R
import com.android254.presentation.common.components.MultiToggleButton
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import com.android254.presentation.sessions.view.SessionsViewModel
import com.droidconke.chai.atoms.MontserratBold
import com.droidconke.chai.atoms.MontserratSemiBold
import com.droidconke.chai.components.CButton
import java.util.*

private fun loadFilters(): List<SessionsFilterOption> {
    return listOf(
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = "Keynote",
            value = "keynote"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = "Codelab",
            value = "codelab"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = "Session",
            value = "Session"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = "Workshop",
            value = "Workshop"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = "Lightning Talk",
            value = "Lightning talk"
        ),
        SessionsFilterOption(
            type = SessionsFilterCategory.SessionType,
            label = "Panel Discussion",
            value = "Panel discussion"
        ),
        SessionsFilterOption(
            label = "Room A",
            value = "Room A",
            type = SessionsFilterCategory.Room
        ),
        SessionsFilterOption(
            label = "Room B",
            value = "Room B",
            type = SessionsFilterCategory.Room
        ),
        SessionsFilterOption(
            label = "Room C",
            value = "Room C",
            type = SessionsFilterCategory.Room
        ),

        SessionsFilterOption(
            label = "Beginner",
            value = "Introductory and overview",
            type = SessionsFilterCategory.Level
        ),
        SessionsFilterOption(
            label = "Intermediate",
            value = "Intermediate",
            type = SessionsFilterCategory.Level
        ),
        SessionsFilterOption(
            label = "advanced",
            value = "Advanced",
            type = SessionsFilterCategory.Level
        ),
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionsFilterPanel(
    onDismiss: () -> Unit,
    onChange: (SessionsFilterOption) -> Unit,
    viewModel: SessionsViewModel
) {
    val filterTypeTextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = MontserratBold
    )

    val selectableFilters = loadFilters()

    val groupedFilters = selectableFilters.groupBy {
        it.type
    }
    val currentSelections = viewModel.selectedFilterOptions.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

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
                viewModel.clearSelectedFilterList()
                onDismiss()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_close_24),
                    contentDescription = stringResource(R.string.close_icon_description)
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                viewModel.clearSelectedFilterList()
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
                    .fillMaxWidth(),
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
                        viewModel.updateSelectedFilterOptionList(it)
                    },
                    currentSelections = currentSelections.value
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        CButton(
            onClick = {
                viewModel.fetchSessionWithFilter()
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