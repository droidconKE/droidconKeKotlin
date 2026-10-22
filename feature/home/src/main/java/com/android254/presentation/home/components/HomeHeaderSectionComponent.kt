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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.android254.presentation.home.viewstate.HomeHero
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.android254.presentation.utils.eventDatesLabel
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.hero.CubeHeroBanner
import ke.droidcon.kotlin.core.ui.R

/** A title runs to this many lines at the hero role before it steps down one. */
private const val HERO_TITLE_LINES = 3

@Composable
fun HomeHeaderSectionComponent(
    modifier: Modifier = Modifier,
    hero: HomeHero = HomeHero.Welcome,
    onFeedbackClick: () -> Unit = {},
    showCube: Boolean = false,
    animateCube: Boolean = true,
) {
    CubeHeroBanner(
        modifier = modifier.testTag("home_header"),
        showCube = showCube,
        animateCube = animateCube,
        onClick = onFeedbackClick.takeIf { hero == HomeHero.ThankYou },
        onClickLabel = stringResource(R.string.hero_thank_you_action),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HeroTitle(text = hero.title())
            HeroLabel(hero.subtitle())
        }
    }
}

/** One title role for every hero state, stepping down a role when the title would run past [HERO_TITLE_LINES] lines. */
@Composable
private fun HeroTitle(text: String) {
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
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun HomeHero.subtitle(): String =
    when (this) {
        HomeHero.Welcome -> stringResource(R.string.home_header_welcome_subtitle)
        HomeHero.ThankYou -> stringResource(R.string.hero_thank_you_action)
        is HomeHero.Countdown -> {
            val first = firstDay
            val last = lastDay
            if (first != null && last != null) {
                val locale = Locale.current.platformLocale
                stringResource(R.string.hero_event_dates, remember(first, last, locale) { eventDatesLabel(first, last, locale) })
            } else {
                stringResource(R.string.home_header_welcome_subtitle)
            }
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
private fun HeroLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.chaiColorsPalette.heroOnContainerColor,
        maxLines = 2,
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