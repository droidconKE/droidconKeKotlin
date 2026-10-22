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
package com.android254.presentation.ticket.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android254.domain.models.Ticket
import com.android254.presentation.ticket.TicketUi
import com.android254.presentation.utils.ChaiLightAndDarkComposePreviews
import com.android254.presentation.utils.eventDatesLabel
import com.droidconke.chai.ChaiTheme
import com.droidconke.chai.chaiColorsPalette
import com.droidconke.chai.icons.ChaiIcons
import ke.droidcon.kotlin.core.ui.R
import kotlinx.datetime.LocalDate

/** The code's stub stays light in dark theme; a dark ground scans badly. */
@Composable
internal fun TicketPass(
    ticket: TicketUi,
    modifier: Modifier = Modifier,
    fitHeight: Boolean = true,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 2.dp,
    ) {
        Column {
            PassHeader(ticket)
            Attendee(ticket)
            Perforation()
            CodeStub(ticket, fitHeight, if (fitHeight) Modifier.weight(1f) else Modifier)
        }
    }
}

@Composable
private fun PassHeader(ticket: TicketUi) {
    val palette = MaterialTheme.chaiColorsPalette
    val locale = Locale.current.platformLocale
    val first = ticket.firstDay
    val last = ticket.lastDay
    val dates = if (first != null && last != null) remember(first, last, locale) { eventDatesLabel(first, last, locale) } else null
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(palette.heroContainerColor)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.ticket_title).uppercase(),
            style = MaterialTheme.typography.labelSmallEmphasized,
            color = palette.heroContentColor,
        )
        Text(
            text = ticket.event,
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = palette.heroOnContainerColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        val place = listOfNotNull(dates, ticket.venue.takeIf(String::isNotBlank))
        if (place.isNotEmpty()) {
            Text(
                text = place.joinToString(stringResource(R.string.detail_separator)),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.heroOnContainerColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Attendee(ticket: TicketUi) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.ticket_attendee).uppercase(),
            style = MaterialTheme.typography.labelSmallEmphasized,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = ticket.attendeeName,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        FlowRow(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (ticket.type.isNotBlank()) {
                PassChip(ticket.type, colors.surfaceContainerHighest, colors.onSurface)
            }
            when {
                ticket.status == Ticket.Status.Refunded ->
                    PassChip(stringResource(R.string.ticket_status_refunded), colors.errorContainer, colors.onErrorContainer)
                ticket.status == Ticket.Status.Revoked ->
                    PassChip(stringResource(R.string.ticket_status_revoked), colors.errorContainer, colors.onErrorContainer)
                ticket.checkedInToday ->
                    PassChip(stringResource(R.string.ticket_status_checked_in), colors.primaryContainer, colors.onPrimaryContainer, ChaiIcons.CheckRounded)
            }
        }
    }
}

@Composable
private fun PassChip(
    text: String,
    container: Color,
    content: Color,
    icon: ImageVector? = null,
) {
    Surface(color = container, contentColor = content, shape = CircleShape) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Perforation() {
    val notch = MaterialTheme.colorScheme.background
    val line = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(24.dp),
    ) {
        val radius = size.height / 2
        drawLine(
            color = line,
            start = Offset(radius * 1.5f, center.y),
            end = Offset(size.width - radius * 1.5f, center.y),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx())),
        )
        drawCircle(notch, radius, Offset(0f, center.y))
        drawCircle(notch, radius, Offset(size.width, center.y))
    }
}

@Composable
private fun CodeStub(
    ticket: TicketUi,
    fitHeight: Boolean,
    modifier: Modifier = Modifier,
) {
    ChaiTheme(darkTheme = false) {
        val colors = MaterialTheme.colorScheme
        val void = !ticket.status.showsCode
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLowest)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    if (fitHeight) {
                        Modifier
                            .weight(1f, fill = false)
                            .widthIn(max = CODE_MAX_SIZE)
                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    } else {
                        Modifier.size(CODE_MAX_SIZE)
                    },
                contentAlignment = Alignment.Center,
            ) {
                EntryCode(
                    qrBase64 = ticket.qrBase64,
                    ticketNumber = ticket.number,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = if (void) VOID_CODE_ALPHA else 1f },
                )
                if (void) {
                    val label = stringResource(if (ticket.status == Ticket.Status.Refunded) R.string.ticket_status_refunded else R.string.ticket_status_revoked)
                    Surface(
                        color = colors.error,
                        contentColor = colors.onError,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.rotate(STAMP_TILT_DEGREES),
                    ) {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.ticket_number_heading).uppercase(),
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = ticket.number,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                color = colors.onSurface,
            )
            Text(
                text =
                    stringResource(
                        when (ticket.status) {
                            Ticket.Status.Refunded -> R.string.ticket_refunded_body
                            Ticket.Status.Revoked -> R.string.ticket_revoked_body
                            Ticket.Status.Active, Ticket.Status.Unknown -> R.string.ticket_show_at_entrance
                        },
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private val CODE_MAX_SIZE = 280.dp
private const val VOID_CODE_ALPHA = 0.18f
private const val STAMP_TILT_DEGREES = -8f

@ChaiLightAndDarkComposePreviews
@Composable
private fun TicketPassPreview() {
    ChaiTheme {
        TicketPass(
            TicketUi(
                number = "TIC-70235",
                attendeeName = "Magak Emmanuel",
                type = "Regular",
                event = "DroidconKe & FlutterconKe 2026",
                firstDay = LocalDate(2026, 11, 5),
                lastDay = LocalDate(2026, 11, 6),
                venue = "PrideInn Azure Hotel Nairobi",
                status = Ticket.Status.Refunded,
                checkedInToday = false,
                qrBase64 = "",
            ),
            modifier = Modifier.size(360.dp, 640.dp),
        )
    }
}