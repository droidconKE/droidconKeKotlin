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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionSpeakersPresentationModel
import com.android254.presentation.models.SessionStatus
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.colors.venueAccentColor
import com.droidconke.chai.components.ChaiBodySmall
import com.droidconke.chai.components.ChaiBodyXSmall
import com.droidconke.chai.components.ChaiSubTitle
import ke.droidcon.kotlin.presentation.R

@Composable
fun SessionsCard(
    session: SessionPresentationModel,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    onBookmark: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val venueAccent = venueAccentColor(session.venue)

    val alpha =
        when (session.sessionStatus) {
            SessionStatus.Past -> 0.5f
            SessionStatus.Ongoing -> 1f
            SessionStatus.Upcoming -> 1f
        }
    val transition = rememberInfiniteTransition(label = "ongoing_border")

    val animatedBorderAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1200,
                        easing = EaseInOut,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "border_alpha",
    )

    val nowTextColor = venueAccent.copy(animatedBorderAlpha)

    val border =
        if (session.sessionStatus == SessionStatus.Ongoing) {
            BorderStroke(
                width = 1.5.dp,
                color = venueAccent.copy(alpha = animatedBorderAlpha),
            )
        } else {
            BorderStroke(width = 1.dp, color = MaterialTheme.chaiColorsPalette.cardsBorderColor)
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .alpha(alpha),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = border,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.chaiColorsPalette.cardsBackground,
            ),
        onClick = { navigateToSessionDetails(session.id) },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            SessionTitleComponent(session, onBookmark)
            if (session.format.isNotBlank() || session.level.isNotBlank() || session.sessionStatus == SessionStatus.Ongoing) {
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (session.format.isNotBlank()) {
                    SessionTag(
                        tagText = session.format,
                        backgroundColor = MaterialTheme.chaiColorsPalette.cardsBorderColor,
                    )
                }
                if (session.level.isNotBlank()) {
                    SessionTag(
                        tagText = session.level,
                        backgroundColor = MaterialTheme.chaiColorsPalette.cardsBorderColor,
                    )
                }
                if (session.sessionStatus == SessionStatus.Ongoing) {
                    SessionTag(
                        tagText = stringResource(R.string.now),
                        isNowTag = true,
                        dotColor = nowTextColor,
                        textColor = nowTextColor,
                        backgroundColor = nowTextColor.copy(alpha = 0.15f),
                    )
                }
            }

            if (session.speakers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                session.speakers.forEach { speaker ->
                    SessionPresenterComponents(speaker = speaker)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.chaiColorsPalette.cardsBorderColor,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ChaiBodyXSmall(
                    bodyText = session.venue.uppercase(),
                    textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                )
                ChaiBodyXSmall(
                    bodyText = "${session.startTime} ${session.amOrPm} - ${session.endTime}",
                    textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                )
            }
        }
    }
}

@Composable
fun SessionTitleComponent(
    session: SessionPresentationModel,
    onBookmark: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChaiSubTitle(
            modifier = Modifier.weight(1f),
            titleText = session.title,
            titleColor = MaterialTheme.chaiColorsPalette.textBoldColor,
        )

        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = {
                onBookmark(session.id)
            },
        ) {
            Crossfade(targetState = session.isStarred, label = "star_crossfade") { isStarred ->
                Icon(
                    imageVector = if (isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                    contentDescription = stringResource(R.string.star_session_icon_description),
                    tint = if (isStarred) ChaiRed else MaterialTheme.chaiColorsPalette.secondaryButtonColor,
                )
            }
        }
    }
}

@Composable
fun SessionPresenterComponents(
    speaker: SessionSpeakersPresentationModel,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = speaker.speakerImage,
            contentDescription = "session speaker image",
            modifier =
                Modifier
                    .size(30.dp)
                    .clip(CircleShape),
        )
        Spacer(modifier = Modifier.width(10.dp))

        ChaiBodySmall(
            bodyText = speaker.name,
            textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
        )
    }
}