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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.utils.ChaiLightAndDarkComposePreview
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.atoms.ChaiTeal90
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiBodySmall
import com.droidconke.chai.components.ChaiBodySmallBold
import ke.droidcon.kotlin.presentation.R

@Composable
fun SpeakerComponent(
    modifier: Modifier = Modifier,
    speaker: SpeakerUI,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .clickable {
                onClick.invoke()
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.chaiColorsPalette.cardsBorderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.chaiColorsPalette.surfaces
        )
    ) {
        ConstraintLayout(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val (image, nameText, bioText, button) = createRefs()
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(speaker.imageUrl)
                    .build(),
                placeholder = painterResource(R.drawable.smiling),
                contentDescription = stringResource(R.string.head_shot),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        border = BorderStroke(
                            2.5.dp,
                            color = ChaiTeal
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .height(120.dp)
                    .width(120.dp)
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }

            )
            ChaiBodyMediumBold(
                modifier = modifier
                    .testTag("name")
                    .constrainAs(nameText) {
                        top.linkTo(image.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .wrapContentHeight(),
                bodyText = speaker.name,
                textColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            ChaiBodySmall(
                modifier = modifier
                    .testTag("bio")
                    .constrainAs(bioText) {
                        top.linkTo(nameText.bottom, margin = 6.dp)
                        bottom.linkTo(button.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .wrapContentHeight(),
                bodyText = speaker.tagline ?: "",
                textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                textAlign = TextAlign.Center,
                maxLines = 3,
                minLines = 3
            )
            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = 2.dp,
                    color = ChaiTeal90
                ),
                modifier = modifier
                    .constrainAs(button) {
                        top.linkTo(bioText.bottom, margin = 28.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                ChaiBodySmallBold(
                    bodyText = stringResource(R.string.session_label).uppercase(),
                    textColor = ChaiTeal90
                )
            }
        }
    }
}

@ChaiLightAndDarkComposePreview
@Composable
fun SpeakerComponentPreview() {
    ChaiDCKE22Theme {
        SpeakerComponent(
            speaker = SpeakerUI(
                imageUrl = "https://sessionize.com/image/09c1-400o400o2-cf-9587-423b-bd2e-415e6757286c.b33d8d6e-1f94-4765-a797-255efc34390d.jpg",
                name = "Harun Wangereka",
                bio = "Kenya Partner Lead at droidcon Berlin | Android | Kotlin | Flutter | C++",
                tagline = "An android engineer | Content creator | Mentor"
            )
        )
    }
}