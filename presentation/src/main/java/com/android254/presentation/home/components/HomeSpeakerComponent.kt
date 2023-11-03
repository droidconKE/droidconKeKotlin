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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.models.SpeakerUI
import com.droidconke.chai.atoms.MontserratMedium
import com.droidconke.chai.chaiColorsPalette
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeSpeakerComponent(speaker: SpeakerUI, onClick: () -> Unit = {}) {
    ConstraintLayout(
        modifier = Modifier
            .width(90.dp)
            .padding(end = 16.dp)
            .clickable { onClick.invoke() }

    ) {
        val (headShot, speakerName) = createRefs()
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(speaker.imageUrl)
                .build(),
            placeholder = painterResource(R.drawable.smiling),
            contentDescription = stringResource(R.string.head_shot),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    border = BorderStroke(
                        2.dp,
                        color = colorResource(id = R.color.cyan)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .height(85.dp)
                .width(85.dp)
                .constrainAs(headShot) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
        Text(
            text = speaker.name,
            modifier = Modifier.constrainAs(speakerName) {
                top.linkTo(headShot.bottom, 10.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            style = TextStyle(
                color = MaterialTheme.chaiColorsPalette.textColorPrimary,
                fontSize = 12.sp,
                fontFamily = MontserratMedium
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun HomeSpeakerComponentPreview() {
    DroidconKE2023Theme {
        Surface(color = Color.White) {
            HomeSpeakerComponent(speaker = SpeakerUI(name = "Harun Wangereka", bio = "Staff Engineer"))
        }
    }
}