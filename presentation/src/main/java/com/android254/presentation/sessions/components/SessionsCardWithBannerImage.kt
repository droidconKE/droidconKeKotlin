/*
 * Copyright 2023 DroidconKE
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

import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.common.components.TimeAndVenueComponent
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionSpeakersPresentationModel
import com.android254.presentation.sessions.view.SessionsViewModel
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.atoms.ChaiTeal
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodySmallBold
import ke.droidcon.kotlin.presentation.R
import kotlinx.coroutines.launch

@Composable
fun SessionsCardWithBannerImage(
    modifier: Modifier = Modifier,
    session: SessionPresentationModel,
    navigateToSessionDetails: (sessionId: String) -> Unit,
    viewModel: SessionsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.chaiColorsPalette.surfaces),
        onClick = { navigateToSessionDetails(session.id) }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(if (session.isService) R.drawable.all else session.sessionImage)
                .build(),
            placeholder = painterResource(R.drawable.all),
            contentDescription = stringResource(id = R.string.session_image),
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TimeAndVenueComponent(session = session)

            Spacer(Modifier.height(12.dp))

            ChaiBodySmallBold(
                bodyText = session.title,
                textColor = MaterialTheme.chaiColorsPalette.textBoldColor
            )
            Spacer(Modifier.height(16.dp))
            SpeakerDetailsAndLikeButtonComponent(
                onBookmarkClicked = {
                    scope.launch {
                        if (session.isStarred) {
                            viewModel.unBookmarkSession(session.id)
                        } else {
                            viewModel.bookmarkSession(session.id)
                        }
                    }
                },
                isSessionStarred = session.isStarred,
                speakers = session.speakers
            )
        }
    }
}

@Composable
fun SpeakerDetailsAndLikeButtonComponent(
    onBookmarkClicked: () -> Unit,
    isSessionStarred: Boolean,
    speakers: List<SessionSpeakersPresentationModel>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically

    ) {
        speakers.forEach { speaker ->
            speaker.speakerImage?.let {
                AsyncImage(
                    model = speaker.speakerImage,
                    contentDescription = "session speaker image",
                    modifier = Modifier
                        .size(32.dp)
                        .border(
                            width = 1.dp,
                            color = ChaiTeal,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            modifier = Modifier.size(32.dp),
            onClick = {
                onBookmarkClicked()
            }
        ) {
            Icon(
                imageVector = if (isSessionStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = stringResource(R.string.star_session_icon_description),
                tint = if (isSessionStarred) ChaiRed else MaterialTheme.chaiColorsPalette.secondaryButtonColor
            )
        }
    }
}