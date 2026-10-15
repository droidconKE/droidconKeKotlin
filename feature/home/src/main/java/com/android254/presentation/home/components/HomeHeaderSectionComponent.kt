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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.LiveBadge
import com.android254.presentation.home.viewstate.HomeHero
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.hero.CubeHeroBanner
import ke.droidcon.kotlin.core.ui.R

/** A title runs to this many lines at the hero role before it steps down one. */
private const val HERO_TITLE_LINES = 3
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
    CubeHeroBanner(
        modifier = modifier.testTag("home_header"),
        showCube = showCube,
        animateCube = animateCube,
        onClick = action,
        onClickLabel = actionLabel,
    ) {
        HeroText(hero, liveSession, nextSession)
    }
}

@Composable
private fun HeroText(
    hero: HomeHero,
    liveSession: SessionPresentationModel?,
    nextSession: SessionPresentationModel?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (liveSession != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiveBadge()
                HeroLabel(stringResource(R.string.hero_live_room_until, liveSession.venue, liveSession.endTime))
            }
        }
        HeroTitle(text = liveSession?.title ?: hero.title(), maxLines = if (liveSession != null) HERO_MAX_LINES else Int.MAX_VALUE)
        HeroSubtitle(hero = hero, liveSession = liveSession, nextSession = nextSession)
    }
}

/**
 * Every hero state shares one title role, the largest that sits well beside the cube. A title that
 * would run past [HERO_TITLE_LINES] lines in the room it has, at any font scale, steps down a role.
 * It is measured first, so it never draws at one size and settles at another.
 */
@Composable
private fun HeroTitle(
    text: String,
    maxLines: Int,
) {
    val large = MaterialTheme.typography.titleLargeEmphasized
    val medium = MaterialTheme.typography.titleMediumEmphasized
    val measurer = rememberTextMeasurer()
    BoxWithConstraints {
        val width = constraints.maxWidth
        val bounded = constraints.hasBoundedWidth
        val fits =
            remember(text, large, width, bounded, measurer) {
                !bounded || measurer.measure(AnnotatedString(text), large, constraints = Constraints(maxWidth = width)).lineCount <= HERO_TITLE_LINES
            }
        Text(
            text = text,
            style = if (fits) large else medium,
            color = MaterialTheme.chaiColorsPalette.heroContentColor,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
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
        hero == HomeHero.Welcome -> HeroLabel(stringResource(R.string.home_header_welcome_subtitle))
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