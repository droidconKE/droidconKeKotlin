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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android254.presentation.models.SessionPresentationModel
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiBodySmallBold
import com.droidconke.chai.components.ChaiTextLabelLarge
import ke.droidcon.kotlin.presentation.R

@Composable
fun HomeSessionSection(
    modifier: Modifier = Modifier,
    sessions: List<SessionPresentationModel>,
    onSessionClick: (sessionId: String) -> Unit,
    onViewAllSessionClicked: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        HomeSectionHeaderComponent(
            sectionLabel = stringResource(id = R.string.sessions_label),
            sectionSize = sessions.size,
            onViewAllClicked = onViewAllSessionClicked
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag("sessions")
        ) {
            items(sessions) { session ->
                HomeSessionContent(
                    session = session,
                    onSessionClick = { onSessionClick(session.id) }
                )
            }
        }
    }
}

@Composable
fun HomeSessionContent(
    session: SessionPresentationModel,
    onSessionClick: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .height(intrinsicSize = IntrinsicSize.Max),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.chaiColorsPalette.surfaces),
        onClick = { onSessionClick(session.id) }
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
            contentScale = ContentScale.FillBounds
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(80.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ChaiBodySmallBold(
                bodyText = session.title,
                textColor = MaterialTheme.chaiColorsPalette.textBoldColor,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))

            ChaiTextLabelLarge(
                bodyText = "@ ${session.startTime} | ${session.venue} ",
                textColor = MaterialTheme.chaiColorsPalette.textWeakColor
            )
        }
    }
}