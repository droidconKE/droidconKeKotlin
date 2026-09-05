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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.models.speakersDummyData
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeSpeakersSection(
    speakers: ImmutableList<SpeakerUI>,
    modifier: Modifier = Modifier,
    navigateToSpeakers: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {},
) {
    // Remembered so the slice is not reallocated on every recomposition.
    val featuredSpeakers = remember(speakers) { speakers.take(MAX_FEATURED_SPEAKERS) }

    Column {
        HomeSectionHeaderComponent(
            sectionLabel = stringResource(id = R.string.speakers_label),
            sectionSize = speakers.size,
            onViewAllClicked = navigateToSpeakers,
        )
        LazyRow(
            modifier =
                modifier
                    .testTag("speakersRow")
                    .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(featuredSpeakers, key = { it.name }) { speaker ->
                HomeSpeakerComponent(speaker = speaker, onClick = {
                    navigateToSpeaker(speaker.name)
                })
            }
        }
    }
}

@Preview
@Composable
private fun HomeSpeakersSectionPreview() {
    ChaiTheme {
        HomeSpeakersSection(speakers = speakersDummyData.toImmutableList())
    }
}

/** The home screen shows a preview row; the full list lives on the speakers screen. */
private const val MAX_FEATURED_SPEAKERS = 8