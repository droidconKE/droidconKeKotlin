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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.models.SpeakerUI
import ke.droidcon.kotlin.presentation.R

@Composable
fun SpeakerComponent(
    modifier: Modifier = Modifier,
    speaker: SpeakerUI,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .padding(7.dp)
            .height(350.dp).clickable {
                onClick.invoke()
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.smoke_white)
        )
    ) {
        ConstraintLayout(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth()
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
                            color = colorResource(id = R.color.cyan)
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
            Text(
                text = speaker.name,
                style = TextStyle(
                    color = colorResource(id = R.color.blue),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.montserrat_bold))
                ),
                modifier = modifier
                    .testTag("name")
                    .constrainAs(nameText) {
                        top.linkTo(image.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
            Text(
                text = speaker.bio ?: "",
                style = TextStyle(
                    color = colorResource(id = R.color.grey),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.montserrat_regular)) // Extract the fonts or get them from chai system
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = modifier
                    .testTag("bio")
                    .constrainAs(bioText) {
                        top.linkTo(nameText.bottom, margin = 8.dp)
                        bottom.linkTo(button.top, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = 2.dp,
                    color = colorResource(id = R.color.aqua)
                ),
                modifier = modifier
                    .constrainAs(button) {
                        top.linkTo(parent.top, margin = 270.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Text(
                    text = stringResource(R.string.session_label),
                    color = colorResource(id = R.color.aqua),
                    fontFamily = FontFamily(Font(R.font.montserrat_semi_bold))
                )
            }
        }
    }
}

@Preview
@Composable
fun SpeakerComponentPreview() {
    DroidconKE2023Theme {
        SpeakerComponent(
            speaker = SpeakerUI(
                imageUrl = "https://sessionize.com/image/09c1-400o400o2-cf-9587-423b-bd2e-415e6757286c.b33d8d6e-1f94-4765-a797-255efc34390d.jpg",
                name = "Harun Wangereka",
                bio = "Kenya Partner Lead at droidcon Berlin | Android | Kotlin | Flutter | C++"
            )
        )
    }
}