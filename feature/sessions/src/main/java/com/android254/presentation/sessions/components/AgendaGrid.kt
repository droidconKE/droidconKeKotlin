/*
 * Copyright 2026 DroidconKE
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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.SessionTag
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionStatus
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.colors.venueAccentColor
import com.droidconke.chai.components.ChaiBodyMediumBold
import com.droidconke.chai.components.ChaiBodyXSmall
import com.droidconke.chai.components.ChaiBodyXSmallBold
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

internal const val AGENDA_GRID_TEST_TAG = "agenda_grid"

private val TimeGutterWidth = 68.dp
private val RoomColumnWidth = 196.dp
private val SlotHeight = 132.dp
private val RoomHeaderHeight = 48.dp

/**
 * The day as a room-by-time grid — the way a schedule is read, and impossible on a phone. The
 * room row and time gutter stay put while the cells scroll.
 */
@Composable
fun AgendaGrid(
    sessionsByTime: ImmutableMap<String, ImmutableList<SessionPresentationModel>>,
    navigateToSessionDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val rooms = rememberAgendaRooms(sessionsByTime)
    val cells = rememberAgendaCells(sessionsByTime)
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    if (rooms.isEmpty()) return

    Row(modifier = modifier.testTag(AGENDA_GRID_TEST_TAG).fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.width(TimeGutterWidth)) {
            Spacer(modifier = Modifier.height(RoomHeaderHeight))
            Column(modifier = Modifier.verticalScroll(verticalScroll)) {
                sessionsByTime.keys.forEach { time ->
                    TimeLabel(time = time)
                }
            }
        }

        Column(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            Row(modifier = Modifier.height(RoomHeaderHeight)) {
                rooms.forEach { room -> RoomHeader(room = room) }
            }
            Column(modifier = Modifier.verticalScroll(verticalScroll)) {
                sessionsByTime.keys.forEach { time ->
                    Row {
                        rooms.forEach { room ->
                            AgendaCell(
                                session = cells[time to room],
                                onClick = navigateToSessionDetails,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Derived from the sessions, not typed by hand: a hardcoded list drifts from the API. */
@Composable
private fun rememberAgendaRooms(
    sessionsByTime: ImmutableMap<String, ImmutableList<SessionPresentationModel>>,
): ImmutableList<String> =
    remember(sessionsByTime) {
        sessionsByTime.values
            .flatten()
            .flatMap { it.roomList }
            .distinct()
            .sorted()
            .toImmutableList()
    }

/** One cell per room, so a two-room session appears under both. */
@Composable
private fun rememberAgendaCells(
    sessionsByTime: ImmutableMap<String, ImmutableList<SessionPresentationModel>>,
): Map<Pair<String, String>, SessionPresentationModel> =
    remember(sessionsByTime) {
        buildMap {
            sessionsByTime.forEach { (time, sessions) ->
                sessions.forEach { session ->
                    session.roomList.forEach { room -> put(time to room, session) }
                }
            }
        }.toImmutableMap()
    }

@Composable
private fun TimeLabel(
    time: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(SlotHeight).width(TimeGutterWidth).padding(end = 8.dp, top = 8.dp),
    ) {
        ChaiBodyXSmallBold(
            bodyText = time,
            textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RoomHeader(
    room: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(RoomColumnWidth).height(RoomHeaderHeight).padding(4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        ChaiBodyXSmallBold(
            bodyText = room.uppercase(),
            textColor = venueAccentColor(room),
        )
    }
}

@Composable
private fun AgendaCell(
    session: SessionPresentationModel?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cell = modifier.width(RoomColumnWidth).height(SlotHeight).padding(4.dp)
    if (session == null) {
        Box(modifier = cell)
        return
    }

    val accent = venueAccentColor(session.venue)
    Column(
        modifier =
            cell
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.chaiColorsPalette.cardsBackground)
                .border(
                    width = if (session.sessionStatus == SessionStatus.Ongoing) 1.5.dp else 1.dp,
                    color =
                        if (session.sessionStatus == SessionStatus.Ongoing) {
                            accent
                        } else {
                            MaterialTheme.chaiColorsPalette.cardsBorderColor
                        },
                    shape = RoundedCornerShape(8.dp),
                ).clickable { onClick(session.id) }
                .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ChaiBodyMediumBold(
            bodyText = session.title,
            textColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor,
            maxLines = 3,
        )
        if (session.format.isNotBlank()) {
            SessionTag(tagText = session.format)
        }
        session.speakers.firstOrNull()?.let { speaker ->
            ChaiBodyXSmall(
                bodyText = speaker.name,
                textColor = MaterialTheme.chaiColorsPalette.textWeakColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun agendaPreviewData(): ImmutableMap<String, ImmutableList<SessionPresentationModel>> =
    remember {
        fakeSessions
            .groupBy { "${it.startTime} ${it.amOrPm}" }
            .mapValues { it.value.toImmutableList() }
            .toImmutableMap()
    }

@androidx.compose.ui.tooling.preview.Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun AgendaGridPreview() {
    ChaiTheme {
        Surface(color = MaterialTheme.chaiColorsPalette.background) {
            AgendaGrid(sessionsByTime = agendaPreviewData(), navigateToSessionDetails = {})
        }
    }
}