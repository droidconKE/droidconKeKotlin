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
package com.android254.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android254.presentation.home.viewstate.HomeViewState
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiBlack
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.atoms.ChaiWhite
import com.droidconke.chai.components.ChaiSubTitle
import com.droidconke.chai.components.ChaiTextLabelMedium
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeBannerSection(homeViewState: HomeViewState) {
    with(homeViewState) {
        if (isPosterVisible) {
            HomeSpacer()
            HomeEventPoster()
        }
        if (isCallForSpeakersVisible) {
            HomeSpacer()
            HomeCallForSpeakersLink()
        }
    }
}

@Composable
fun HomeEventPoster() {
    Image(
        painter = painterResource(id = R.drawable.droidcon_event_banner),
        contentDescription = stringResource(id = R.string.home_banner_event_poster_description),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("home_event_poster"),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun HomeCallForSpeakersLink() {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4.2f)
            .testTag("home_call_for_speakers_link"),
        colors = CardDefaults.cardColors(containerColor = ChaiTeal)
    ) {
        Row(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home_speakers_card_drawable),
                contentDescription = stringResource(id = R.string.home_banner_call_for_speakers_image_description),
                modifier = Modifier.padding(start = 15.dp, end = 19.dp),
                tint = Color.Unspecified
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                ChaiSubTitle(
                    titleText = stringResource(id = R.string.home_banner_call_for_speakers_label),
                    titleColor = ChaiWhite
                )
                ChaiTextLabelMedium(
                    bodyText = stringResource(id = R.string.home_banner_call_for_speakers_apply_to_speak_label),
                    textColor = ChaiBlack
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_home_speakers_card_play),
                contentDescription = stringResource(id = R.string.home_banner_call_for_speakers_image_description),
                modifier = Modifier.align(Alignment.CenterVertically),
                tint = ChaiWhite
            )
        }
    }
}

@Preview
@Composable
fun HomeBannerSectionPreview() {
    ChaiDCKE22Theme {
        HomeBannerSection(HomeViewState())
    }
}

@Preview
@Composable
fun HomeEventBannerPreview() {
    ChaiDCKE22Theme {
        HomeEventPoster()
    }
}

@Preview
@Composable
fun HomeCallForSpeakersLinkPreview() {
    ChaiDCKE22Theme {
        HomeCallForSpeakersLink()
    }
}