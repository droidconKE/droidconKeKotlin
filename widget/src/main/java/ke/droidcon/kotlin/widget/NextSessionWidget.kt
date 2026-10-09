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

import android.content.Context
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
import kotlin.time.Clock

class NextSessionWidget(
    private val sessionsRepo: SessionsRepo,
) : GlanceAppWidget() {
    override val sizeMode =
        SizeMode.Responsive(
            setOf(SmallWidget, MediumWidget, LargeWidget),
        )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            val now = Clock.System.now().toEpochMilliseconds()
            val current by sessionsRepo.fetchCurrentSessions(now).collectAsState(emptyList())
            val next by sessionsRepo.fetchUpNextSessions(now).collectAsState(emptyList())
            val launchIntent = remember { context.packageManager.getLaunchIntentForPackage(context.packageName) }

            GlanceTheme {
                WidgetContent(
                    current = current.firstOrNull(),
                    next = next.firstOrNull(),
                    size = LocalSize.current,
                    launchIntent = launchIntent,
                )
            }
        }
    }

    private companion object {
        val SmallWidget = DpSize(140.dp, 100.dp)
        val MediumWidget = DpSize(250.dp, 100.dp)
        val LargeWidget = DpSize(250.dp, 200.dp)
    }
}