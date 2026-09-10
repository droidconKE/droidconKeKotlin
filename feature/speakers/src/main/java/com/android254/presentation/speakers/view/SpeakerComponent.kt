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
package com.android254.presentation.speakers.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.navigation.speakerSharedImage
import com.android254.presentation.common.navigation.speakerSharedName
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodyXSmallBold
import ke.droidcon.kotlin.core.ui.R

private val SpeakerAvatarSize = 100.dp
private val SpeakerAvatarShape = RoundedCornerShape(16.dp)

@Composable
fun SpeakerComponent(
    speaker: SpeakerUI,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick.invoke() }
                .semantics(mergeDescendants = true) {}
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(speaker.imageUrl)
                    .build(),
            placeholder = painterResource(R.drawable.smiling),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(SpeakerAvatarSize)
                    .speakerSharedImage(speaker.name, SpeakerAvatarShape),
        )

        Column(
            modifier =
                Modifier
                    .padding(start = 20.dp)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ChaiBodyLargeBold(
                modifier =
                    Modifier
                        .testTag("name")
                        .speakerSharedName(speaker.name),
                bodyText = speaker.name,
                textColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor,
                maxLines = 1,
            )

            speaker.tagline?.takeIf(String::isNotBlank)?.let { tagline ->
                ChaiBodyMedium(
                    modifier = Modifier.testTag("bio"),
                    bodyText = tagline,
                    textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                    maxLines = 2,
                )
            }

            if (speaker.isSpeakingNow) {
                Spacer(modifier = Modifier.height(2.dp))
                SpeakingNowBadge()
            }
        }
    }
}

@Composable
private fun SpeakingNowBadge(modifier: Modifier = Modifier) {
    ChaiBodyXSmallBold(
        modifier =
            modifier
                .testTag("speakingNowBadge")
                .border(
                    border = BorderStroke(1.dp, ChaiTeal),
                    shape = RoundedCornerShape(6.dp),
                ).padding(horizontal = 12.dp, vertical = 6.dp),
        bodyText = stringResource(R.string.speaking_now_label).uppercase(),
        textColor = ChaiTeal,
    )
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun SpeakerComponentPreview() {
    ChaiTheme {
        SpeakerComponent(
            speaker =
                SpeakerUI(
                    imageUrl = "https://sessionize.com/image/09c1-400o400o2-cf-9587-423b-bd2e-415e6757286c.b33d8d6e-1f94-4765-a797-255efc34390d.jpg",
                    name = "Ian Nthuli",
                    bio = "Kenya Partner Lead at droidcon Berlin | Android | Kotlin | Flutter | C++",
                    tagline = "Android Engineer",
                    isSpeakingNow = true,
                ),
        )
    }
}