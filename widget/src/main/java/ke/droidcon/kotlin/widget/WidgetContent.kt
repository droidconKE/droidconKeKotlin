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
package ke.droidcon.kotlin.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.android254.domain.models.Session

/**
 * The large breakpoint (250x200dp) is the only one tall enough for a two-line title plus a
 * richer, speaker-inclusive detail line; small/medium (both 100dp tall) stay compact.
 */
private val LargeBreakpointMinHeight = 150.dp

/** How far an "up next" row is indented when it's shown alongside a live session - see
 * [WidgetContent]. */
private val UpNextIndent = 20.dp

private enum class SessionKind { CURRENT, UP_NEXT }

/**
 * Renders the widget as a single scrollable list: every currently-running session first (each
 * with its own venue-accent rail and "LIVE" pill), followed by every upcoming session. When a
 * live session is also shown, the upcoming ones are indented and run at a smaller type scale, so
 * "live" and "up next" read as visually distinct within the same list. With nothing live, the
 * upcoming sessions are the only content and stand on their own, so they stay flush and
 * full-size - there's nothing to visually distinguish them from. Nothing is hidden behind a
 * count anymore - if more sessions exist than fit the widget's height, the list scrolls
 * natively. Falls back to a plain empty-state message when there is nothing to show.
 */
@Composable
fun WidgetContent(
    current: List<Session>,
    next: List<Session>,
    size: DpSize,
    launchIntent: Intent?,
) {
    val isLarge = size.height >= LargeBreakpointMinHeight
    val rootModifier =
        GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.widgetBackground)

    Box(modifier = rootModifier) {
        if (current.isEmpty() && next.isEmpty()) {
            Column(modifier = GlanceModifier.padding(12.dp)) {
                EmptyStateContent()
            }
        } else {
            // Up-next rows are indented (and run smaller) only when a live session is also
            // shown, so live/up-next read as visually distinct. Up-next alone stays flush and
            // full-size, since there's nothing to distinguish it from.
            val indentUpNext = current.isNotEmpty()
            LazyColumn(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
                items(current, itemId = { sessionItemId(SessionKind.CURRENT, it) }) { session ->
                    SessionRow(
                        session = session,
                        kind = SessionKind.CURRENT,
                        isLarge = isLarge,
                        isIndented = false,
                        launchIntent = launchIntent,
                    )
                }
                items(next, itemId = { sessionItemId(SessionKind.UP_NEXT, it) }) { session ->
                    SessionRow(
                        session = session,
                        kind = SessionKind.UP_NEXT,
                        isLarge = isLarge,
                        isIndented = indentUpNext,
                        launchIntent = launchIntent,
                    )
                }
            }
        }
    }
}

private fun sessionItemId(
    kind: SessionKind,
    session: Session,
): Long = (kind.name + session.id).hashCode().toLong()

/** A session's room accent, mirroring the app's `venueAccentColor`. Room names change yearly,
 * so an unknown room falls back to the neutral role rather than a fixed hue. */
internal enum class VenueAccent { SECONDARY, TERTIARY, NEUTRAL }

internal fun venueAccent(rooms: String): VenueAccent {
    val primaryRoom =
        rooms
            .split(',')
            .firstOrNull()
            ?.trim()
            .orEmpty()
    return when {
        primaryRoom.equals("Opal", ignoreCase = true) -> VenueAccent.SECONDARY
        primaryRoom.equals("Sapphire", ignoreCase = true) -> VenueAccent.TERTIARY
        else -> VenueAccent.NEUTRAL
    }
}

@Composable
private fun VenueAccent.toColorProvider(): ColorProvider =
    when (this) {
        VenueAccent.SECONDARY -> GlanceTheme.colors.secondary
        VenueAccent.TERTIARY -> GlanceTheme.colors.tertiary
        VenueAccent.NEUTRAL -> GlanceTheme.colors.onSurfaceVariant
    }

private fun speakerNames(session: Session): String = session.speakers.joinToString(", ") { it.name }

private fun sessionDetailText(
    session: Session,
    kind: SessionKind,
    isLarge: Boolean,
): String {
    val speakers = speakerNames(session)
    if (!isLarge || speakers.isEmpty()) {
        return if (kind == SessionKind.CURRENT) session.rooms else "${session.startTime} · ${session.rooms}"
    }
    val trailingDetail = if (kind == SessionKind.CURRENT) "ends ${session.endTime}" else session.startTime
    return "$speakers · ${session.rooms} · $trailingDetail"
}

private data class PillStyle(
    val containerColor: ColorProvider,
    val contentColor: ColorProvider,
    val dotColor: ColorProvider,
)

@Composable
private fun pillStyleFor(
    kind: SessionKind,
    session: Session,
): PillStyle =
    when (kind) {
        SessionKind.CURRENT ->
            PillStyle(
                containerColor = GlanceTheme.colors.secondaryContainer,
                contentColor = GlanceTheme.colors.onSecondaryContainer,
                dotColor = GlanceTheme.colors.onSecondaryContainer,
            )
        SessionKind.UP_NEXT ->
            PillStyle(
                containerColor = GlanceTheme.colors.primaryContainer,
                contentColor = GlanceTheme.colors.onPrimaryContainer,
                dotColor = venueAccent(session.rooms).toColorProvider(),
            )
    }

/** Fully-rounded pill/chip corner radius. Oversized on purpose — Android clamps a corner radius
 * bigger than half an element's own size, which is the standard trick for a guaranteed stadium
 * shape regardless of the pill's exact content-driven height. */
private val PillCornerRadius = 50.dp

@Composable
private fun SessionRow(
    session: Session,
    kind: SessionKind,
    isLarge: Boolean,
    isIndented: Boolean,
    launchIntent: Intent?,
) {
    val detailText = sessionDetailText(session, kind, isLarge)
    val pillStyle = pillStyleFor(kind, session)
    val statusTextRes =
        if (kind == SessionKind.CURRENT) R.string.widget_happening_now else R.string.widget_up_next
    val detailColor = venueAccent(session.rooms).toColorProvider()
    val titleFontSize =
        when {
            isIndented && isLarge -> 16.sp
            isIndented -> 13.sp
            isLarge -> 18.sp
            else -> 15.sp
        }
    val detailFontSize = if (isIndented) 11.sp else 13.sp

    val rowModifier =
        GlanceModifier
            .fillMaxWidth()
            .padding(start = if (isIndented) UpNextIndent else 0.dp, bottom = 10.dp)
            .let { modifier ->
                if (launchIntent != null) {
                    modifier.clickable(actionStartActivity(launchIntent))
                } else {
                    modifier
                }
            }

    Row(modifier = rowModifier) {
        // Venue-accent rail, sized to just this row - not the whole widget.
        Box(
            modifier =
                GlanceModifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .cornerRadius(PillCornerRadius)
                    .background(detailColor),
        ) {}
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column {
            StatusPill(
                text = LocalContext.current.getString(statusTextRes),
                containerColor = pillStyle.containerColor,
                contentColor = pillStyle.contentColor,
                dotColor = pillStyle.dotColor,
                textModifier = GlanceModifier.semantics { testTag = "statusLabel" },
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            Text(
                text = session.title,
                modifier = GlanceModifier.semantics { testTag = "sessionTitle" },
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = titleFontSize),
                maxLines = 2,
            )
            Text(
                text = detailText,
                modifier = GlanceModifier.semantics { testTag = "sessionDetail" },
                style = TextStyle(color = detailColor, fontSize = detailFontSize),
                maxLines = if (isLarge) 2 else 1,
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    containerColor: ColorProvider,
    contentColor: ColorProvider,
    dotColor: ColorProvider,
    textModifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier =
            GlanceModifier
                .cornerRadius(PillCornerRadius)
                .background(containerColor)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = GlanceModifier.size(6.dp).cornerRadius(3.dp).background(dotColor)) {}
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = text.uppercase(),
            modifier = textModifier,
            style = TextStyle(color = contentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp),
        )
    }
}

@Composable
private fun EmptyStateContent() {
    Text(
        text = LocalContext.current.getString(R.string.widget_no_sessions),
        modifier = GlanceModifier.semantics { testTag = "emptyState" },
        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
    )
}