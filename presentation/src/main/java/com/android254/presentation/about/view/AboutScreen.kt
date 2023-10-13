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
package com.android254.presentation.about.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.common.components.OrganizedBySection
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.models.OrganizingTeamMember
import com.droidconke.chai.atoms.MontserratBold
import com.droidconke.chai.atoms.MontserratRegular
import ke.droidcon.kotlin.presentation.R

@Composable
fun AboutRoute(
    aboutViewModel: AboutViewModel = hiltViewModel(),
    navigateToFeedbackScreen: () -> Unit = {}
) {
    val uiState by aboutViewModel.uiState.collectAsStateWithLifecycle()

    AboutScreen(
        uiState = uiState,
        navigateToFeedbackScreen = navigateToFeedbackScreen
    )
}

@Composable
private fun AboutScreen(
    uiState: AboutScreenUiState,
    navigateToFeedbackScreen: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            DroidconAppBarWithFeedbackButton(
                onButtonClick = {
                    navigateToFeedbackScreen()
                },
                userProfile = ""
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is AboutScreenUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize()

                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            is AboutScreenUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            is AboutScreenUiState.Success -> {
                val teamMembers = uiState.teamMembers
                val stakeHolderLogos = uiState.stakeHoldersLogos
                Column(
                    Modifier
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .testTag("about_screen")
                ) {
                    AboutDroidconSection(
                        droidconDesc = stringResource(id = R.string.about_droidcon),
                        droidconImage = ""
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    OrganizingTeamSection(
                        modifier = Modifier,
                        organizingTeam = teamMembers,
                        onClickMember = {
                            // TODO navigate to team screen
                        }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    OrganizedBySection(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                        organizationLogos = stakeHolderLogos
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun AboutDroidconSection(
    modifier: Modifier = Modifier,
    droidconDesc: String,
    droidconImage: String
) {
    Column(
        modifier = modifier
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.team)
                .build(),
            placeholder = painterResource(R.drawable.team),
            contentDescription = stringResource(id = R.string.about_droidcon_image),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp),
            text = stringResource(id = R.string.about),
            style = TextStyle(
                color = MaterialTheme.colorScheme.surfaceTint,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontFamily = MontserratBold
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp),
            text = droidconDesc,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontFamily = MontserratRegular
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizingTeamSection(
    modifier: Modifier = Modifier,
    organizingTeam: List<OrganizingTeamMember>,
    onClickMember: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .padding(start = 20.dp, end = 20.dp)
            .testTag("organizing_team_section")
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.about_organizing_team),
            style = TextStyle(
                color = MaterialTheme.colorScheme.surfaceTint,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontFamily = MontserratBold
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            organizingTeam.forEach { teamMember ->
                OrganizingTeamComponent(
                    modifier = Modifier.width(106.dp),
                    teamMember = teamMember,
                    onClickMember = onClickMember
                )
            }
        }
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AboutScreenPreview() {
    DroidconKE2023Theme {
        AboutScreen(
            uiState = AboutScreenUiState.Success(
                teamMembers = listOf(
                    OrganizingTeamMember(
                        name = "Member 1",
                        desc = "Description 1",
                        image = ""
                    ),
                    OrganizingTeamMember(
                        name = "Member 2",
                        desc = "Description 2",
                        image = ""
                    ),
                    OrganizingTeamMember(
                        name = "Member 3",
                        desc = "Description 3",
                        image = ""
                    ),
                    OrganizingTeamMember(
                        name = "Member 4",
                        desc = "Description 4",
                        image = ""
                    ),
                    OrganizingTeamMember(
                        name = "Member 5",
                        desc = "Description 5",
                        image = ""
                    ),
                    OrganizingTeamMember(
                        name = "Member 6",
                        desc = "Description 6",
                        image = ""
                    )
                ),
                stakeHoldersLogos = listOf(
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                )
            )
        )
    }
}