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
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.android254.domain.models.Session

/**
 * Renders the widget's three states: a session running now, a session coming up next, or
 * nothing scheduled.
 *
 * [size] is not read yet: this task's three states don't branch on it. It's part of the public
 * signature now because `NextSessionWidget.provideGlance`, the test suite and the previews
 * below already pass their breakpoint through, so a later size-aware layout change doesn't need
 * a signature change.
 */
@Suppress("UnusedParameter")
@Composable
fun WidgetContent(
    current: Session?,
    next: Session?,
    size: DpSize,
    launchIntent: Intent?,
) {
    val rootModifier =
        GlanceModifier
            .fillMaxSize()
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
            current != null -> HappeningNowContent(current)
            next != null -> UpNextContent(next)
            else -> EmptyStateContent()
        }
    }
}

@Composable
private fun HappeningNowContent(session: Session) {
    StatusAndSessionContent(
        statusText = LocalContext.current.getString(R.string.widget_happening_now),
        statusColor = GlanceTheme.colors.error,
        session = session,
        detailText = session.rooms,
    )
}

@Composable
private fun UpNextContent(session: Session) {
    StatusAndSessionContent(
        statusText = LocalContext.current.getString(R.string.widget_up_next),
        statusColor = GlanceTheme.colors.primary,
        session = session,
        detailText = "${session.startTime} · ${session.rooms}",
    )
}

@Composable
private fun StatusAndSessionContent(
    statusText: String,
    statusColor: ColorProvider,
    session: Session,
    detailText: String,
) {
    Column {
        Text(
            text = statusText,
            modifier = GlanceModifier.semantics { testTag = "statusLabel" },
            style = TextStyle(color = statusColor),
        )
        Text(
            text = session.title,
            modifier = GlanceModifier.semantics { testTag = "sessionTitle" },
            maxLines = 2,
        )
        Text(text = detailText)
    }
}

@Composable
private fun EmptyStateContent() {
    Text(
        text = LocalContext.current.getString(R.string.widget_no_sessions),
        modifier = GlanceModifier.semantics { testTag = "emptyState" },
    )
}