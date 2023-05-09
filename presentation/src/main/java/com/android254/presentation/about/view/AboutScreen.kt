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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.components.DroidconAppBarWithFeedbackButton
import com.android254.presentation.common.components.OrganizedBySection
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.models.OrganizingTeamMember
import com.droidconke.chai.atoms.ChaiBlue
import com.droidconke.chai.atoms.ChaiCoal
import com.droidconke.chai.atoms.MontserratBold
import com.droidconke.chai.atoms.MontserratRegular
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import ke.droidcon.kotlin.presentation.R

@Composable
fun AboutScreen(
    aboutViewModel: AboutViewModel = hiltViewModel(),
    navigateToFeedbackScreen: () -> Unit = {}
) {
    val teamMembers = remember { mutableStateListOf<OrganizingTeamMember>() }
    val stakeHolderLogos = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val data = aboutViewModel.getOrganizers()
        teamMembers.addAll(data.first)
        stakeHolderLogos.addAll(data.second)
    }

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
                color = ChaiBlue,
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
                color = ChaiCoal,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontFamily = MontserratRegular
            )
        )
    }
}

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
                color = ChaiBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontFamily = MontserratBold
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        FlowRow(
            modifier = Modifier,
            mainAxisAlignment = MainAxisAlignment.SpaceBetween,
            mainAxisSize = SizeMode.Expand,
            mainAxisSpacing = 16.dp,
            crossAxisSpacing = 16.dp,
            lastLineMainAxisAlignment = MainAxisAlignment.Start
        ) {
            organizingTeam.forEach { teamMember ->
                OrganizingTeamComponent(
                    modifier = Modifier.width(99.dp),
                    teamMember = teamMember,
                    onClickMember = onClickMember
                )
            }
        }
    }
}

@Preview
@Composable
fun AboutScreenPreview() {
    DroidconKE2023Theme {
        AboutScreen()
    }
}