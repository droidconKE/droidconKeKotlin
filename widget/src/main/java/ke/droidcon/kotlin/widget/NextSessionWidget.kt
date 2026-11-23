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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.android254.domain.repos.SessionsRepo
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Clock

/**
 * Takes the repo as a function because Glance also builds the receiver by reflection, with nothing
 * injected, just to read this class's name; only [provideGlance] may touch the repo.
 */
class NextSessionWidget(
    private val sessionsRepo: () -> SessionsRepo,
) : GlanceAppWidget() {
    override val sizeMode =
        SizeMode.Responsive(
            setOf(SmallWidget, MediumWidget, LargeWidget),
        )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val repo = sessionsRepo()
        val now = Clock.System.now().toEpochMilliseconds()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        provideContent {
            val currentSessionsFlow = remember(now) { repo.fetchCurrentSessions(now) }
            val upNextSessionsFlow = remember(now) { repo.fetchUpNextSessions(now) }
            val current by currentSessionsFlow.collectAsState(emptyList())
            val next by upNextSessionsFlow.collectAsState(emptyList())

            GlanceTheme {
                WidgetContent(
                    current = current.toImmutableList(),
                    next = next.toImmutableList(),
                    size = LocalSize.current,
                    launchIntent = launchIntent,
                )
            }
        }
    }

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable,
    ) {
        Log.e(TAG, "Failed to compose NextSessionWidget (appWidgetId=$appWidgetId)", throwable)
        val fallback =
            RemoteViews(context.packageName, android.R.layout.simple_list_item_1).apply {
                setTextViewText(android.R.id.text1, context.getString(R.string.widget_error_fallback))
            }
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, fallback)
    }

    private companion object {
        const val TAG = "NextSessionWidget"
        val SmallWidget = DpSize(140.dp, 100.dp)
        val MediumWidget = DpSize(250.dp, 100.dp)
        val LargeWidget = DpSize(250.dp, 200.dp)
    }
}