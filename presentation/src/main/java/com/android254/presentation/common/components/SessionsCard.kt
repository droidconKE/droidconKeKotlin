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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionSpeakersPresentationModel
import com.android254.presentation.sessions.view.SessionsViewModel
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodyLargeBold
import com.droidconke.chai.components.ChaiBodyMedium
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiBodySmall
import com.droidconke.chai.components.ChaiBodyXSmall
import com.droidconke.chai.components.ChaiSubTitle
import ke.droidcon.kotlin.presentation.R
import kotlinx.coroutines.launch

@Composable
fun SessionsCard(
    session: SessionPresentationModel,
    navigateToSessionDetails: (sessionId: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.chaiColorsPalette.cardsBackground),
        onClick = { navigateToSessionDetails(session.id) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            SessionTimeComponent(
                session.startTime,
                session.amOrPm
            )
            Spacer(modifier = Modifier.width(24.dp))
            SessionDetails(session = session)
        }
    }
}

@Composable
fun RowScope.SessionTimeComponent(sessionStartTime: String, sessionAmOrPm: String) {
    Column(
        modifier = Modifier
            .weight(0.2f),
        horizontalAlignment = Alignment.End
    ) {
        ChaiBodyLargeBold(
            bodyText = sessionStartTime,
            textColor = MaterialTheme.chaiColorsPalette.textBoldColor
        )

        ChaiBodyMediumBold(
            bodyText = sessionAmOrPm,
            textColor = MaterialTheme.chaiColorsPalette.textBoldColor
        )
    }
}

@Composable
fun RowScope.SessionDetails(session: SessionPresentationModel) {
    Column(
        modifier = Modifier
            .weight(0.8f)
    ) {
        SessionTitleComponent(session)
        Spacer(modifier = Modifier.height(12.dp))
        SessionsDescriptionComponent(session.description)
        Spacer(modifier = Modifier.height(12.dp))
        TimeAndVenueComponent(session)
        Spacer(modifier = Modifier.height(12.dp))
        if (session.speakers.isNotEmpty()) {
            Column {
                session.speakers.forEach { speaker ->
                    SessionPresenterComponents(speaker = speaker)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SessionTitleComponent(
    session: SessionPresentationModel,
    viewModel: SessionsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Center
    ) {
        ChaiSubTitle(
            modifier = Modifier.weight(1f),
            titleText = session.title,
            titleColor = MaterialTheme.chaiColorsPalette.textBoldColor
        )

        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = {
                scope.launch {
                    if (session.isStarred) {
                        viewModel.unBookmarkSession(session.remoteId)
                    } else {
                        viewModel.bookmarkSession(session.remoteId)
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (session.isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = stringResource(R.string.star_session_icon_description),
                tint = if (session.isStarred) ChaiRed else MaterialTheme.chaiColorsPalette.secondaryButtonColor
            )
        }
    }
}

@Composable
fun SessionsDescriptionComponent(sessionDescription: String) {
    ChaiBodyMedium(
        bodyText = sessionDescription,
        textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
        maxLines = 3
    )
}

@Composable
fun TimeAndVenueComponent(session: SessionPresentationModel) {
    Row() {
        ChaiBodyXSmall(
            bodyText = "${session.startTime} - ${session.endTime}",
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        ChaiBodyXSmall(
            bodyText = "|",
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        ChaiBodyXSmall(
            bodyText = session.venue.uppercase(),
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor
        )
    }
}

@Composable
fun SessionPresenterComponents(
    speaker: SessionSpeakersPresentationModel
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = speaker.speakerImage,
            contentDescription = "session speaker image",
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))

        ChaiBodySmall(
            bodyText = speaker.name,
            textColor = MaterialTheme.chaiColorsPalette.textLabelAndHeadings
        )
    }
}