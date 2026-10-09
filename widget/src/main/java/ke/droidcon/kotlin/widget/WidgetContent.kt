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
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
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
 * Renders the widget's three states: one or more sessions running now, one or more sessions
 * coming up next, or nothing scheduled. When more than one session qualifies for a state, the
 * first is shown in full and the rest are surfaced as an "+N more" count rather than silently
 * dropped, since conference tracks run in parallel.
 *
 * [size] is not read yet: this task's states don't branch on it. It's part of the public
 * signature now because `NextSessionWidget.provideGlance`, the test suite and the previews
 * below already pass their breakpoint through, so a later size-aware layout change doesn't need
 * a signature change.
 */
@Suppress("UnusedParameter")
@Composable
fun WidgetContent(
    current: List<Session>,
    next: List<Session>,
    size: DpSize,
    launchIntent: Intent?,
) {
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

    Column(modifier = rootModifier) {
        when {
            current.isNotEmpty() -> HappeningNowContent(current.first(), extraCount = current.size - 1)
            next.isNotEmpty() -> UpNextContent(next.first(), extraCount = next.size - 1)
            else -> EmptyStateContent()
        }
    }
}

@Composable
private fun HappeningNowContent(
    session: Session,
    extraCount: Int,
) {
    StatusAndSessionContent(
        statusText = LocalContext.current.getString(R.string.widget_happening_now),
        containerColor = GlanceTheme.colors.secondaryContainer,
        contentColor = GlanceTheme.colors.onSecondaryContainer,
        showDot = true,
        session = session,
        detailText = session.rooms,
        extraCount = extraCount,
        extraCountLabelRes = R.string.widget_more_live,
    )
}

@Composable
private fun UpNextContent(
    session: Session,
    extraCount: Int,
) {
    StatusAndSessionContent(
        statusText = LocalContext.current.getString(R.string.widget_up_next),
        containerColor = GlanceTheme.colors.primaryContainer,
        contentColor = GlanceTheme.colors.onPrimaryContainer,
        showDot = false,
        session = session,
        detailText = "${session.startTime} · ${session.rooms}",
        extraCount = extraCount,
        extraCountLabelRes = R.string.widget_more_next,
    )
}

@Composable
private fun StatusAndSessionContent(
    statusText: String,
    containerColor: ColorProvider,
    contentColor: ColorProvider,
    showDot: Boolean,
    session: Session,
    detailText: String,
    extraCount: Int,
    extraCountLabelRes: Int,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusPill(
                text = statusText,
                containerColor = containerColor,
                contentColor = contentColor,
                showDot = showDot,
                textModifier = GlanceModifier.semantics { testTag = "statusLabel" },
            )
            if (extraCount > 0) {
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = LocalContext.current.getString(extraCountLabelRes, extraCount),
                    modifier = GlanceModifier.semantics { testTag = "extraSessionsLabel" },
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
                )
            }
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = session.title,
            modifier = GlanceModifier.semantics { testTag = "sessionTitle" },
            style = TextStyle(fontWeight = FontWeight.Bold),
            maxLines = 2,
        )
        Text(
            text = detailText,
            modifier = GlanceModifier.semantics { testTag = "sessionDetail" },
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
            maxLines = 1,
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    containerColor: ColorProvider,
    contentColor: ColorProvider,
    showDot: Boolean,
    textModifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier =
            GlanceModifier
                .cornerRadius(10.dp)
                .background(containerColor)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDot) {
            Box(modifier = GlanceModifier.size(6.dp).cornerRadius(3.dp).background(contentColor)) {}
            Spacer(modifier = GlanceModifier.width(4.dp))
        }
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