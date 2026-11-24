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
package com.android254.presentation.sessionDetails.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.adaptive.DroidconTooltip
import com.android254.presentation.common.adaptive.clickablePointer
import com.android254.presentation.common.components.BookmarkButton
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.droidconke.chai.components.ChaiTextLabelLarge
import com.droidconke.chai.components.ChaiTitle
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R

@Composable
fun SessionSpeakerNameAndFavouriteIcon(
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAddToCalendar: (() -> Unit)? = null,
) {
    val speakers = sessionDetails.speakers
    Column(modifier = modifier) {
        if (speakers.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = ChaiIcons.Android,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .height(14.dp)
                            .width(15.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )

                Spacer(modifier = Modifier.width(6.dp))

                ChaiTextLabelLarge(
                    bodyText = pluralStringResource(id = R.plurals.speaker_label, count = speakers.size),
                    textColor = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (speakers.isEmpty()) Arrangement.End else Arrangement.spacedBy(8.dp),
        ) {
            if (speakers.isNotEmpty()) {
                ChaiTitle(
                    modifier = Modifier.weight(1f).testTag(TestTag.SPEAKER_NAME),
                    titleText = speakers.joinToString(" & ") { it.name },
                    titleColor = MaterialTheme.colorScheme.primary,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onAddToCalendar != null) {
                    CalendarActionButton(onClick = onAddToCalendar)
                }
                BookmarkButton(
                    isBookmarked = sessionDetails.isStarred,
                    onToggle = {
                        if (sessionDetails.isStarred) {
                            unBookmarkSession(sessionDetails.id)
                        } else {
                            bookmarkSession(sessionDetails.id)
                        }
                    },
                    modifier = Modifier.testTag(TestTag.FAVOURITE_ICON),
                )
            }
        }
    }
}

/** Matches [BookmarkButton]'s 36dp circular tonal container inside a 48dp touch target. */
@Composable
private fun CalendarActionButton(onClick: () -> Unit) {
    val label = stringResource(R.string.add_to_calendar)
    DroidconTooltip(text = label, modifier = Modifier.testTag(TestTag.ADD_TO_CALENDAR_BUTTON)) {
        Box(
            modifier =
                Modifier
                    .sizeIn(minWidth = MIN_TOUCH_TARGET, minHeight = MIN_TOUCH_TARGET)
                    .clip(CircleShape)
                    .clickablePointer()
                    .clickable(role = Role.Button, onClick = onClick)
                    .semantics { contentDescription = label },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(ACTION_CONTAINER_SIZE)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ChaiIcons.EventOutlined,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(ACTION_ICON_SIZE),
                )
            }
        }
    }
}

private val MIN_TOUCH_TARGET = 48.dp
private val ACTION_CONTAINER_SIZE = 36.dp
private val ACTION_ICON_SIZE = 20.dp