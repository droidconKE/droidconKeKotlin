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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.adaptive.DroidconWindowSize
import com.android254.presentation.common.adaptive.rememberDroidconWindowSize
import com.android254.presentation.common.components.LiveBadge
import com.android254.presentation.home.viewstate.HomeHero
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.hero.CubeHero
import ke.droidcon.kotlin.core.ui.R

internal const val LARGE_FONT_SCALE = 1.5f
private val HeroCubeHeight = 200.dp
private const val HERO_MAX_LINES = 5

@Composable
fun HomeHeaderSectionComponent(
    modifier: Modifier = Modifier,
    hero: HomeHero = HomeHero.Welcome,
    liveSession: SessionPresentationModel? = null,
    nextSession: SessionPresentationModel? = null,
    onSessionClick: (String) -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    showCube: Boolean = false,
    animateCube: Boolean = true,
) {
    val featured = liveSession
    val action: (() -> Unit)? =
        when {
            featured != null -> ({ onSessionClick(featured.id) })
            hero == HomeHero.ThankYou -> onFeedbackClick
            else -> null
        }
    val actionLabel = if (featured != null) stringResource(R.string.open_session) else stringResource(R.string.hero_thank_you_action)
    Row(
        modifier =
            modifier
                .testTag("home_header")
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.chaiColorsPalette.heroContainerColor)
                .then(
                    if (action != null) {
                        Modifier.clickable(onClickLabel = actionLabel, role = Role.Button, onClick = action)
                    } else {
                        Modifier
                    },
                ).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        HeroText(hero, liveSession, nextSession, Modifier.weight(1f))
        if (showCube && rememberDroidconWindowSize() != DroidconWindowSize.Compact) {
            CubeHero(
                animate = animateCube,
                modifier = Modifier.weight(1f).height(HeroCubeHeight).testTag("cube_hero"),
            )
        }
    }
}

@Composable
private fun HeroText(
    hero: HomeHero,
    liveSession: SessionPresentationModel?,
    nextSession: SessionPresentationModel?,
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.chaiColorsPalette
    val largeFont = LocalDensity.current.fontScale > LARGE_FONT_SCALE
    val displayStyle =
        if (liveSession != null && !largeFont) {
            MaterialTheme.typography.headlineMediumEmphasized
        } else {
            MaterialTheme.typography.titleLargeEmphasized
        }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (liveSession != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiveBadge()
                HeroLabel(stringResource(R.string.hero_live_room_until, liveSession.venue, liveSession.endTime))
            }
        }
        Text(
            text = liveSession?.title ?: hero.title(),
            style = displayStyle,
            color = palette.heroContentColor,
            maxLines = if (liveSession != null) HERO_MAX_LINES else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        HeroSubtitle(hero = hero, liveSession = liveSession, nextSession = nextSession)
    }
}

@Composable
private fun HeroSubtitle(
    hero: HomeHero,
    liveSession: SessionPresentationModel?,
    nextSession: SessionPresentationModel?,
) {
    when {
        liveSession != null && nextSession != null ->
            HeroLabel(stringResource(R.string.hero_up_next, "${nextSession.startTime} ${nextSession.amOrPm}", nextSession.title), maxLines = 1)
        liveSession != null -> Unit
        hero is HomeHero.Countdown -> HeroLabel(stringResource(R.string.hero_countdown_label))
        hero == HomeHero.ThankYou -> HeroLabel(stringResource(R.string.hero_thank_you_action))
    }
}

@Composable
private fun HomeHero.title(): String =
    when (this) {
        HomeHero.Welcome -> stringResource(R.string.home_header_welcome_label)
        HomeHero.ThankYou -> stringResource(R.string.hero_thank_you)
        is HomeHero.Countdown ->
            if (daysUntil == 0) {
                stringResource(R.string.hero_starts_today)
            } else {
                pluralStringResource(R.plurals.hero_days_to_go, daysUntil, daysUntil)
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