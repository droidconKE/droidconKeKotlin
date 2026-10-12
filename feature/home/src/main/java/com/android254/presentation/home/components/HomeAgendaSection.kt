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

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.LiveBadge
import com.android254.presentation.home.viewstate.AgendaDay
import com.android254.presentation.home.viewstate.AgendaItem
import com.android254.presentation.home.viewstate.HomeAgenda
import com.android254.presentation.models.SessionStatus
import com.droidconke.chai.colors.venueAccentColor
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeAgendaSection(
    agenda: HomeAgenda,
    onSessionClick: (String) -> Unit,
    onBrowseSessions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().testTag("home_agenda")) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            HomeSectionTitle(stringResource(R.string.agenda_title), Modifier.padding(end = 12.dp))
            Text(
                text = agenda.day.label(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        if (agenda.items.isEmpty()) {
            EmptyAgenda(onBrowseSessions)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AgendaGap)) {
                agenda.items.forEachIndexed { index, item ->
                    AgendaRow(
                        item = item,
                        shape = segmentShape(index, agenda.items.size),
                        onClick = { onSessionClick(item.session.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAgenda(onBrowseSessions: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.agenda_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(onClick = onBrowseSessions) {
                Text(stringResource(R.string.agenda_browse_sessions))
            }
        }
    }
}

@Composable
private fun AgendaRow(
    item: AgendaItem,
    shape: Shape,
    onClick: () -> Unit,
) {
    val session = item.session
    val isLive = session.sessionStatus == SessionStatus.Ongoing
    val largeFont = LocalDensity.current.fontScale > LARGE_FONT_SCALE
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = shape,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (largeFont) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TimePill(time = "${session.startTime} ${session.amOrPm}", isLive = isLive)
                AgendaDetails(item)
            }
        } else {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimeTile(time = session.startTime, period = session.amOrPm, isLive = isLive)
                AgendaDetails(item, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AgendaDetails(
    item: AgendaItem,
    modifier: Modifier = Modifier,
) {
    val session = item.session
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = session.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            if (session.sessionStatus == SessionStatus.Ongoing) LiveBadge()
            Text(
                text = session.venue,
                style = MaterialTheme.typography.labelMedium,
                color = venueAccentColor(session.venue),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        item.clashesWith.forEach { title -> ClashTag(title) }
    }
}

@Composable
private fun TimePill(
    time: String,
    isLive: Boolean,
) {
    Text(
        text = time,
        style = MaterialTheme.typography.titleSmallEmphasized,
        color = if (isLive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier
                .background(
                    if (isLive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                    MaterialTheme.shapes.medium,
                ).padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Every tile is as wide as the widest time, so titles line up; only the live one is neon. */
@Composable
private fun TimeTile(
    time: String,
    period: String,
    isLive: Boolean,
) {
    val style = MaterialTheme.typography.titleMediumEmphasized
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val width =
        remember(style, density) {
            with(density) {
                measurer
                    .measure(WIDEST_TIME, style)
                    .size.width
                    .toDp()
            } + 16.dp
        }
    val container = if (isLive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    val content = if (isLive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
    Column(
        modifier =
            Modifier
                .width(width)
                .background(container, MaterialTheme.shapes.medium)
                .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = time, style = style, color = content, maxLines = 1)
        Text(text = period, style = MaterialTheme.typography.labelSmall, color = content, maxLines = 1)
    }
}

@Composable
private fun ClashTag(title: String) {
    Row(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ChaiIcons.ErrorRounded,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.agenda_clashes_with, title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Expressive connected list: large outer corners, small inner ones. */
@Composable
private fun segmentShape(
    index: Int,
    count: Int,
): Shape {
    val outer = MaterialTheme.shapes.extraLarge.topStart
    val inner = MaterialTheme.shapes.extraSmall.topStart
    val top = if (index == 0) outer else inner
    val bottom = if (index == count - 1) outer else inner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom)
}

@Composable
private fun AgendaDay.label(): String =
    when (this) {
        AgendaDay.Today -> stringResource(R.string.agenda_today)
        AgendaDay.Tomorrow -> stringResource(R.string.agenda_tomorrow)
        is AgendaDay.On -> {
            val locale = Locale.current.platformLocale
            remember(date, locale) {
                val pattern = DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM")
                date.toJavaLocalDate().format(DateTimeFormatter.ofPattern(pattern, locale))
            }
        }
    }

private val AgendaGap = 2.dp
private const val WIDEST_TIME = "00:00"
private const val LARGE_FONT_SCALE = 1.5f