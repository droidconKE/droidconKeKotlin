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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.models.speakersDummyData
import com.droidconke.chai.ChaiDCKE22Theme
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeSpeakersSection(
    speakers: List<SpeakerUI>,
    navigateToSpeakers: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {}
) {
    Column {
        HomeSectionHeaderComponent(
            sectionLabel = stringResource(id = R.string.speakers_label),
            sectionSize = speakers.size,
            onViewAllClicked = navigateToSpeakers
        )
        LazyRow(
            modifier = Modifier
                .testTag("speakersRow")
                .padding(top = 24.dp)
        ) {
            items(speakers.take(8)) { speaker ->
                HomeSpeakerComponent(speaker = speaker, onClick = {
                    navigateToSpeaker(speaker.name)
                })
            }
        }
    }
}

@Preview
@Composable
fun HomeSpeakersSectionPreview() {
    ChaiDCKE22Theme {
        HomeSpeakersSection(speakers = speakersDummyData)
    }
}