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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android254.presentation.common.navigation.sessionSharedImage
import com.android254.presentation.common.navigation.sessionSharedTitle
import com.android254.presentation.models.SessionPresentationModel
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList

@Composable
fun HomeSessionSection(
    sessions: ImmutableList<SessionPresentationModel>,
    onSessionClick: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
    onViewAllSessionClicked: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        HomeSectionHeaderComponent(
            sectionLabel = stringResource(id = R.string.sessions_label),
            sectionSize = sessions.size,
            onViewAllClicked = onViewAllSessionClicked,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag("sessions"),
        ) {
            items(sessions, key = { it.id }) { session ->
                HomeSessionContent(
                    session = session,
                    onSessionClick = { onSessionClick(session.id) },
                )
            }
        }
    }
}

@Composable
fun HomeSessionContent(
    session: SessionPresentationModel,
    onSessionClick: (sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontScale = LocalDensity.current.fontScale
    Card(
        modifier = modifier.width(SessionCardWidth * fontScale.coerceAtLeast(1f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        onClick = { onSessionClick(session.id) },
    ) {
        AsyncImage(
            model = if (session.isService) R.drawable.all else session.sessionImage,
            placeholder = painterResource(R.drawable.all),
            contentDescription = stringResource(id = R.string.session_image),
            modifier =
                Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .sessionSharedImage(
                        session.id,
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    ),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier =
                Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = session.title,
                modifier = Modifier.sessionSharedTitle(session.id),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "@ ${session.startTime} | ${session.venue}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// Grows with the font scale so a title wraps at word breaks rather than mid-word.
private val SessionCardWidth = 140.dp
