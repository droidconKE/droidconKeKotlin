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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
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
    viewportHeight: Dp = 0.dp,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 2.dp,
    ) {
        Layout(
            content = {
                PassHeader(ticket)
                Attendee(ticket)
                Perforation()
                CodeStub(ticket)
            },
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val fixed = measurables.dropLast(1).map { it.measure(Constraints(minWidth = width, maxWidth = width)) }
            val fixedHeight = fixed.sumOf { it.height }
            val stubHeight = maxOf(viewportHeight.roundToPx() - fixedHeight, measurables.last().minIntrinsicHeight(width))
            val stub = measurables.last().measure(Constraints.fixed(width, stubHeight))
            layout(width, fixedHeight + stubHeight) {
                var y = 0
                (fixed + stub).forEach { placeable ->
                    placeable.placeRelative(0, y)
                    y += placeable.height
                }
            }
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
            text = ticket.event,
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = palette.heroOnContainerColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        listOfNotNull(dates, ticket.venue.takeIf(String::isNotBlank)).forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.heroOnContainerColor,
                maxLines = 1,
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
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(text = text, style = MaterialTheme.typography.labelMedium)
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
    modifier: Modifier = Modifier,
) {
    ChaiTheme(darkTheme = false) {
        val colors = MaterialTheme.colorScheme
        val void = !ticket.status.showsCode
        Layout(
            modifier =
                modifier
                    .background(colors.surfaceContainerLowest)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            content = {
                Box(contentAlignment = Alignment.Center) {
                    EntryCode(
                        qrBase64 = ticket.qrBase64,
                        ticketNumber = ticket.number,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = if (void) VOID_CODE_ALPHA else 1f },
                    )
                    if (void) VoidStamp(ticket.status)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            },
            measurePolicy = codeStubMeasurePolicy,
        )
    }
}

/** The code takes what the texts leave, between a scannable floor and its full size, and the pair sits centred. */
private val codeStubMeasurePolicy =
    object : MeasurePolicy {
        override fun MeasureScope.measure(
            measurables: List<Measurable>,
            constraints: Constraints,
        ): MeasureResult {
            val width = constraints.maxWidth
            val texts = measurables[1].measure(Constraints(maxWidth = width))
            val spacing = CODE_TEXT_SPACING.roundToPx()
            val room = if (constraints.hasBoundedHeight) constraints.maxHeight - texts.height - spacing else Int.MAX_VALUE
            val codeSize = room.coerceIn(CODE_MIN_SIZE.roundToPx(), minOf(CODE_MAX_SIZE.roundToPx(), width))
            val code = measurables[0].measure(Constraints.fixed(codeSize, codeSize))
            val height = maxOf(constraints.minHeight, codeSize + spacing + texts.height)
            return layout(width, height) {
                val top = (height - (codeSize + spacing + texts.height)) / 2
                code.placeRelative((width - codeSize) / 2, top)
                texts.placeRelative((width - texts.width) / 2, top + codeSize + spacing)
            }
        }

        override fun IntrinsicMeasureScope.minIntrinsicHeight(
            measurables: List<IntrinsicMeasurable>,
            width: Int,
        ): Int = CODE_MIN_SIZE.roundToPx() + CODE_TEXT_SPACING.roundToPx() + measurables[1].minIntrinsicHeight(width)
    }

@Composable
private fun VoidStamp(status: Ticket.Status) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.error,
        contentColor = colors.onError,
        shape = MaterialTheme.shapes.medium,
        modifier =
            Modifier
                .wrapContentWidth(unbounded = true)
                .rotate(STAMP_TILT_DEGREES),
    ) {
        Text(
            text = stringResource(if (status == Ticket.Status.Refunded) R.string.ticket_status_refunded else R.string.ticket_status_revoked).uppercase(),
            style = MaterialTheme.typography.titleMediumEmphasized,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

private val CODE_MAX_SIZE = 280.dp
private val CODE_MIN_SIZE = 160.dp
private val CODE_TEXT_SPACING = 12.dp
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
            viewportHeight = 640.dp,
        )
    }
}