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
package com.android254.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.LiveBadge
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import ke.droidcon.kotlin.core.ui.R

private const val LARGE_FONT_SCALE = 1.5f
private const val HERO_MAX_LINES = 5

@Composable
fun HomeHeaderSectionComponent(
    modifier: Modifier = Modifier,
    liveSession: SessionPresentationModel? = null,
    nextSession: SessionPresentationModel? = null,
    onSessionClick: (String) -> Unit = {},
) {
    val palette = MaterialTheme.chaiColorsPalette
    val openLabel = stringResource(R.string.open_session)
    val featured = liveSession
    val largeFont = LocalDensity.current.fontScale > LARGE_FONT_SCALE
    val displayStyle =
        if (featured != null && !largeFont) {
            MaterialTheme.typography.headlineMediumEmphasized
        } else {
            MaterialTheme.typography.titleLargeEmphasized
        }
    Column(
        modifier =
            modifier
                .testTag("home_header")
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(palette.heroContainerColor)
                .then(
                    if (featured != null) {
                        Modifier.clickable(onClickLabel = openLabel, role = Role.Button) { onSessionClick(featured.id) }
                    } else {
                        Modifier
                    },
                ).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (liveSession != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiveBadge()
                HeroLabel(stringResource(R.string.hero_live_room_until, liveSession.venue, liveSession.endTime))
            }
        }
        Text(
            text = featured?.title ?: stringResource(id = R.string.home_header_welcome_label),
            style = displayStyle,
            color = palette.heroContentColor,
            maxLines = HERO_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        if (liveSession != null && nextSession != null) {
            HeroLabel(stringResource(R.string.hero_up_next, "${nextSession.startTime} ${nextSession.amOrPm}", nextSession.title), maxLines = 1)
        }
    }
}

@Composable
private fun HeroLabel(
    text: String,
    maxLines: Int = 2,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.chaiColorsPalette.heroOnContainerColor,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@ChaiLightAndDarkComposePreviews
@Composable
private fun HomeHeaderSectionComponentPreview() {
    ChaiTheme {
        HomeHeaderSectionComponent()
    }
}