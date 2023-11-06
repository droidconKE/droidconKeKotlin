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

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.models.SponsorPresentationModel
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiSubTitle
import ke.droidcon.kotlin.presentation.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SponsorsCard(
    modifier: Modifier = Modifier,
    sponsors: List<SponsorPresentationModel>
) {
    Card {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.chaiColorsPalette.surfaces,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 30.dp, vertical = 10.dp)
                .testTag("sponsors_section"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChaiSubTitle(
                modifier = Modifier.fillMaxWidth(),
                titleText = stringResource(id = R.string.sponsors_title),
                titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.Start,
                maxItemsInEachRow = 3
            ) {
                sponsors.forEach { sponsor ->
                    val customModifier = if (sponsor.sponsorType.equals("platinum", ignoreCase = true)) {
                        Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                    } else {
                        Modifier
                            .weight(0.5f)
                            .height(50.dp)
                    }
                    val logo = if (isSystemInDarkTheme()) sponsor.logo.replace(".png", "-dark.png") else sponsor.logo
                    AsyncImage(
                        modifier = customModifier,
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(logo)
                            .crossfade(true)
                            .build(),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(R.drawable.ic_google_logo_icon),
                        contentDescription = stringResource(id = R.string.logo)
                    )
                }
            }
        }
    }
}