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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
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
 * richer, speaker-inclusive detail line (and a corner flourish); small/medium (both 100dp tall)
 * stay compact.
 */
private val LargeBreakpointMinHeight = 150.dp

/**
 * Renders the widget's three states: one or more sessions running now, one or more sessions
 * coming up next, or nothing scheduled. When more than one session qualifies for a state, the
 * first is shown in full and the rest are surfaced as an "+N more" count rather than silently
 * dropped, since conference tracks run in parallel.
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
            .padding(12.dp)
            .let { modifier ->
                if (launchIntent != null) {
                    modifier.clickable(actionStartActivity(launchIntent))
                } else {
                    modifier
                }
            }

    Box(modifier = rootModifier) {
        if (isLarge) {
            CornerFlourish()
        }
        Column {
            when {
                current.isNotEmpty() -> HappeningNowContent(current.first(), extraCount = current.size - 1, size = size)
                next.isNotEmpty() -> UpNextContent(next.first(), extraCount = next.size - 1, size = size)
                else -> EmptyStateContent()
            }
        }
    }
}

/**
 * A small decorative flourish pinned to the bottom-right corner, behind the real content.
 * Glance's `Box` shares one `contentAlignment` across all its children, so pinning just this
 * layer to a corner (while the content layer stays top-start) uses the standard
 * weighted-spacer push instead: a [Column] and [Row], each with a `defaultWeight()` spacer
 * before the image, land it bottom-right without disturbing the sibling content layer.
 */
@Composable
private fun CornerFlourish() {
    Column(modifier = GlanceModifier.fillMaxHeight()) {
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(R.drawable.ic_widget_confetti),
                contentDescription = null,
                modifier = GlanceModifier.size(40.dp).semantics { testTag = "cornerFlourish" },
            )
        }
    }
}

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

@Composable
private fun HappeningNowContent(
    session: Session,
    extraCount: Int,
    size: DpSize,
) {
    val isLarge = size.height >= LargeBreakpointMinHeight
    val speakers = speakerNames(session)
    val detailText =
        if (isLarge && speakers.isNotEmpty()) {
            "$speakers · ${session.rooms} · ends ${session.endTime}"
        } else {
            session.rooms
        }
    StatusAndSessionContent(
        statusText = LocalContext.current.getString(R.string.widget_happening_now),
        pillStyle =
            PillStyle(
                containerColor = GlanceTheme.colors.secondaryContainer,
                contentColor = GlanceTheme.colors.onSecondaryContainer,
                dotColor = GlanceTheme.colors.onSecondaryContainer,
            ),
        session = session,
        detailText = detailText,
        detailColor = venueAccent(session.rooms).toColorProvider(),
        detailMaxLines = if (isLarge) 2 else 1,
        titleFontSize = if (isLarge) 18.sp else 15.sp,
        extraCount = extraCount,
        extraCountLabelRes = R.string.widget_more_live,
    )
}

@Composable
private fun UpNextContent(
    session: Session,
    extraCount: Int,
    size: DpSize,
) {
    val isLarge = size.height >= LargeBreakpointMinHeight
    val speakers = speakerNames(session)
    val detailText =
        if (isLarge && speakers.isNotEmpty()) {
            "$speakers · ${session.rooms} · ${session.startTime}"
        } else {
            "${session.startTime} · ${session.rooms}"
        }
    StatusAndSessionContent(
        statusText = LocalContext.current.getString(R.string.widget_up_next),
        pillStyle =
            PillStyle(
                containerColor = GlanceTheme.colors.primaryContainer,
                contentColor = GlanceTheme.colors.onPrimaryContainer,
                dotColor = venueAccent(session.rooms).toColorProvider(),
            ),
        session = session,
        detailText = detailText,
        detailColor = venueAccent(session.rooms).toColorProvider(),
        detailMaxLines = if (isLarge) 2 else 1,
        titleFontSize = if (isLarge) 18.sp else 15.sp,
        extraCount = extraCount,
        extraCountLabelRes = R.string.widget_more_next,
    )
}

private data class PillStyle(
    val containerColor: ColorProvider,
    val contentColor: ColorProvider,
    val dotColor: ColorProvider,
)

/** Fully-rounded pill/chip corner radius. Oversized on purpose — Android clamps a corner radius
 * bigger than half an element's own size, which is the standard trick for a guaranteed stadium
 * shape regardless of the pill's exact content-driven height. */
private val PillCornerRadius = 50.dp

@Composable
private fun StatusAndSessionContent(
    statusText: String,
    pillStyle: PillStyle,
    session: Session,
    detailText: String,
    detailColor: ColorProvider,
    detailMaxLines: Int,
    titleFontSize: TextUnit,
    extraCount: Int,
    extraCountLabelRes: Int,
) {
    Row {
        // Venue-accent rail: a thin stadium-capped stripe tying the card to its room's color.
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(
                    text = statusText,
                    containerColor = pillStyle.containerColor,
                    contentColor = pillStyle.contentColor,
                    dotColor = pillStyle.dotColor,
                    textModifier = GlanceModifier.semantics { testTag = "statusLabel" },
                )
                if (extraCount > 0) {
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Row(
                        modifier =
                            GlanceModifier
                                .cornerRadius(PillCornerRadius)
                                .background(GlanceTheme.colors.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = LocalContext.current.getString(extraCountLabelRes, extraCount),
                            modifier = GlanceModifier.semantics { testTag = "extraSessionsLabel" },
                            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
                        )
                    }
                }
            }
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
                style = TextStyle(color = detailColor),
                maxLines = detailMaxLines,
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