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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.models.speakersDummyData
import com.droidconke.chai.atoms.MontserratBold
import com.droidconke.chai.atoms.MontserratRegular
import com.droidconke.chai.atoms.MontserratSemiBold
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeSpeakersSection(
    speakers: List<SpeakerUI>,
    navigateToSpeakers: () -> Unit = {},
    navigateToSpeaker: (String) -> Unit = {}
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val (titleText, viewAllBtn, speakersRow) = createRefs()
        Text(
            text = stringResource(id = R.string.speakers_label),
            style = TextStyle(
                color = colorResource(id = R.color.blue),
                fontSize = 20.sp,
                fontFamily = MontserratBold
            ),
            modifier = Modifier
                .testTag("speakersLabel")
                .constrainAs(titleText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(speakersRow.top)
                }
        )
        TextButton(
            onClick = navigateToSpeakers,
            modifier = Modifier
                .testTag("viewAllBtn")
                .constrainAs(viewAllBtn) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(speakersRow.top)
                }
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.view_all_label),
                    style = TextStyle(
                        color = colorResource(id = R.color.blue),
                        fontSize = 12.sp,
                        fontFamily = MontserratSemiBold
                    )
                )
                Spacer(modifier = Modifier.width(7.dp))
                Box(
                    modifier = Modifier
                        .height(22.dp)
                        .width(35.dp)
                        .background(
                            color = colorResource(id = R.color.light_blue),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Text(
                        text = "${speakers.size}",
                        modifier = Modifier.align(Alignment.Center),
                        style = TextStyle(
                            color = colorResource(id = R.color.blue),
                            fontSize = 10.sp,
                            fontFamily = MontserratRegular
                        )
                    )
                }
            }
        }
        LazyRow(
            modifier = Modifier
                .testTag("speakersRow")
                .padding(vertical = 22.dp)
                .constrainAs(speakersRow) {
                    top.linkTo(titleText.bottom)
                }
        ) {
            items(speakers.take(8)) { speaker ->
                HomeSpeakerComponent(speaker = speaker, onClick = {
                    navigateToSpeaker("${speaker.id}")
                })
            }
        }
    }
}

@Preview
@Composable
fun HomeSpeakersSectionPreview() {
    Surface(color = Color.White) {
        HomeSpeakersSection(speakers = speakersDummyData)
    }
}