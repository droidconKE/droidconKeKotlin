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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.LiveBadge
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionStatus
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.atoms.LARGE_FONT_SCALE
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R

/** A nudge, not a summary: what is on, when, and one tap to it. Your own session wears a star. */
@Composable
fun CurrentSessionComponent(
    session: SessionPresentationModel,
    modifier: Modifier = Modifier,
    onClicked: (String) -> Unit = {},
) {
    val isNow = session.sessionStatus == SessionStatus.Ongoing
    val palette = MaterialTheme.chaiColorsPalette
    val largeFont = LocalDensity.current.fontScale > LARGE_FONT_SCALE
    val time = if (isNow) stringResource(R.string.rail_until, session.endTime) else "${session.startTime} ${session.amOrPm}"

    Surface(
        onClick = { onClicked(session.id) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = palette.heroContainerColor,
        contentColor = palette.heroOnContainerColor,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isNow) {
                        LiveBadge()
                    } else {
                        Text(
                            text = stringResource(R.string.up_next).uppercase(),
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            color = palette.heroContentColor,
                        )
                    }
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.heroOnContainerColor.copy(alpha = 0.78f),
                        maxLines = 1,
                    )
                }
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = if (largeFont) 4 else 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (session.isStarred) {
                Icon(
                    imageVector = ChaiIcons.StarRounded,
                    contentDescription = stringResource(R.string.rail_your_session),
                    tint = palette.heroContentColor,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CurrentSessionUpNextPreview() {
    val defaultSession = fakeSessions.first()
    val session = fakeSessions.find { session -> session.sessionStatus == SessionStatus.Upcoming } ?: defaultSession

    ChaiTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            CurrentSessionComponent(
                session = session,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CurrentSessionCurrentPreview() {
    val defaultSession = fakeSessions.first()
    val session = fakeSessions.find { session -> session.sessionStatus == SessionStatus.Ongoing } ?: defaultSession

    ChaiTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            CurrentSessionComponent(
                session = session,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}