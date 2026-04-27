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
package com.android254.presentation.sessions.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionSpeakersPresentationModel
import com.droidconke.chai.ChaiDCKE22Theme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodySmallBold
import ke.droidcon.kotlin.presentation.R
import java.util.Locale

@Composable
fun CurrentSessionComponent(
    session: SessionPresentationModel,
    isNow: Boolean,
    modifier: Modifier = Modifier,
    onClicked: (String) -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "throbbingColor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        onClick = { onClicked(session.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.chaiColorsPalette.cardsBackground
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = session.speakers.firstOrNull()?.speakerImage,
                    contentDescription = "Speaker image",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape),
                    contentScale = ContentScale.Crop
                )
                // Status Indicator Dot
                if (isNow) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = 2.dp, y = (-2).dp)
                            .background(session.color.copy(alpha), CircleShape)
                            .border(2.dp, MaterialTheme.chaiColorsPalette.cardsBackground, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    ChaiBodyLargeBold(
                        bodyText = session.title,
                        textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                session.color.copy(alpha = if (isNow) 0.15f * alpha else 0.15f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        ChaiBodySmallBold(
                            bodyText =
                                if (isNow) stringResource(R.string.now).capitalize(Locale.ROOT)
                                else stringResource(R.string.up_next).capitalize(Locale.ROOT),
                            textColor = session.color
                        )
                    }
                }
                val speakerInfo = session.speakers.joinToString(", ") { it.name }
                ChaiBodyMedium(
                    bodyText = "$speakerInfo • ${session.venue}",
                    textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                    maxLines = 1
                )
            }
        }
    }
}

class CurrentSessionPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

@PreviewLightDark
@Composable
fun CurrentSessionPreview(
    @PreviewParameter(CurrentSessionPreviewParameterProvider::class) isNow: Boolean
) {
    val session = SessionPresentationModel(
        id = "1",
        title = "Modern Android Development",
        description = "Keynote description",
        venue = "Main Hall",
        startTime = "09:00",
        endTime = "10:00 AM",
        amOrPm = "AM",
        isStarred = false,
        format = "Keynote",
        level = "Beginner",
        startDate = "2023-11-17 09:00:00",
        endDate = "2023-11-17 10:00:00",
        remoteId = "1",
        eventDay = "1",
        speakers = listOf(
            SessionSpeakersPresentationModel(
                name = "Lisa F Temecula",
                speakerImage = "",
                twitterHandle = "lisa"
            )
        )
    )
    ChaiDCKE22Theme {
        Surface(
            color = MaterialTheme.chaiColorsPalette.background,
        ){
            CurrentSessionComponent(
                session = session,
                isNow = isNow,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
