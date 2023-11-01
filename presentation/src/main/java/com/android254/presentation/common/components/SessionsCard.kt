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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionSpeakersPresentationModel
import com.android254.presentation.sessions.view.SessionsViewModel
import com.droidconke.chai.atoms.MontserratBold
import com.droidconke.chai.atoms.MontserratSemiBold
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
        shape = RoundedCornerShape(5),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        onClick = { navigateToSessionDetails(session.id) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(PaddingValues(top = 20.dp, bottom = 8.dp))
        ) {
            SessionTimeComponent(
                session.startTime,
                session.amOrPm
            )
            Spacer(modifier = Modifier.width(8.dp))
            SessionDetails(session = session)
        }
    }
}

@Composable
fun RowScope.SessionTimeComponent(sessionStartTime: String, sessionAmOrPm: String) {
    Column(
        modifier = Modifier.weight(0.15f),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = sessionStartTime,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontFamily = MontserratSemiBold
            )
        )
        Text(
            text = sessionAmOrPm,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontFamily = MontserratSemiBold
            )
        )
    }
}

@Composable
fun RowScope.SessionDetails(session: SessionPresentationModel) {
    Column(
        modifier = Modifier
            .weight(0.85f)
            .padding(PaddingValues(start = 10.dp, end = 10.dp, bottom = 10.dp))
    ) {
        SessionTitleComponent(session)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = session.level.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                style = TextStyle(fontFamily = MontserratSemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (session.format.isNotEmpty() && session.level.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "|",
                    fontSize = 12.sp,
                    style = TextStyle(fontFamily = MontserratSemiBold)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = session.format.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                style = TextStyle(fontFamily = MontserratSemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        SessionsDescriptionComponent(session.description)
        Spacer(modifier = Modifier.height(20.dp))
        TimeAndVenueComponent(session)
        Spacer(modifier = Modifier.height(12.dp))
        if (session.speakers.isNotEmpty()) {
            Column {
                session.speakers.forEach { speaker ->
                    SessionPresenterComponents(speaker = speaker)
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
        Modifier.fillMaxWidth()
    ) {
        Text(
            text = session.title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = MontserratBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            scope.launch {
                if (session.isStarred) {
                    viewModel.unBookmarkSession(session.id)
                } else {
                    viewModel.bookmarkSession(session.id)
                }
            }
        }) {
            Icon(
                imageVector = if (session.isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = stringResource(R.string.star_session_icon_description),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SessionsDescriptionComponent(sessionDescription: String) {
    Text(
        text = sessionDescription,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 5,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TimeAndVenueComponent(session: SessionPresentationModel) {
    Row() {
        Text(
            text = "${session.startTime} - ${session.endTime}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "|",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = session.venue.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SessionPresenterComponents(speaker: SessionSpeakersPresentationModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = speaker.speakerImage,
            contentDescription = "session speaker image",
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = speaker.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontFamily = MontserratSemiBold
            )
        )
    }
}