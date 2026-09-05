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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.droidconke.chai.atoms.ChaiRed
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.components.ChaiTextLabelLarge
import com.droidconke.chai.components.ChaiTitle
import ke.droidcon.kotlin.core.ui.R

@Composable
fun SessionSpeakerNameAndFavouriteIcon(
    sessionDetails: SessionDetailsPresentationModel,
    bookmarkSession: (String) -> Unit,
    unBookmarkSession: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Android,
                contentDescription = null,
                modifier =
                    Modifier
                        .height(14.dp)
                        .width(15.dp),
                tint = ChaiRed,
            )

            Spacer(modifier = Modifier.width(6.dp))

            ChaiTextLabelLarge(
                bodyText = stringResource(id = R.string.speaker_label),
                textColor = ChaiRed,
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ChaiTitle(
                modifier = Modifier.testTag(TestTag.SPEAKER_NAME),
                titleText = sessionDetails.speakers.joinToString(" & ") { it.name },
                titleColor = MaterialTheme.chaiColorsPalette.textTitlePrimaryColor,
            )

            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = {
                    if (sessionDetails.isStarred) {
                        unBookmarkSession(sessionDetails.id)
                    } else {
                        bookmarkSession(sessionDetails.id)
                    }
                },
            ) {
                Icon(
                    modifier =
                        Modifier
                            .testTag(TestTag.FAVOURITE_ICON),
                    imageVector = if (sessionDetails.isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                    contentDescription = stringResource(R.string.star_session_icon_description),
                    tint = if (sessionDetails.isStarred) ChaiRed else MaterialTheme.chaiColorsPalette.secondaryButtonColor,
                )
            }
        }
    }
}