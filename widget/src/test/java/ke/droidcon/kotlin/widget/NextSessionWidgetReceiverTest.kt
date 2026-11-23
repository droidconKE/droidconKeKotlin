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
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.TextView
import androidx.glance.GlanceId
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NextSessionWidgetReceiverTest {
    private class StubWidgetProvider : AppWidgetProvider()

    /** Glance does this to record which widget a receiver serves, on every `updateAll`. */
    @Test
    fun `a receiver built by reflection names its widget without an injected repo`() {
        val receiver = NextSessionWidgetReceiver::class.java.getDeclaredConstructor().newInstance()

        assertEquals(NextSessionWidget::class.java, receiver.glanceAppWidget.javaClass)
    }

    @Test
    fun `onCompositionError installs a text-only fallback RemoteViews`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val manager = AppWidgetManager.getInstance(context)
        val widgetId =
            shadowOf(manager).createWidget(
                StubWidgetProvider::class.java,
                android.R.layout.simple_list_item_1,
            )
        val widget = NextSessionWidget { error("repo must not be called on error fallback") }

        widget.onCompositionError(
            context = context,
            glanceId = object : GlanceId {},
            appWidgetId = widgetId,
            throwable = IllegalStateException("simulated composition failure"),
        )

        val fallbackView = shadowOf(manager).getViewFor(widgetId)
        val text = fallbackView.findViewById<TextView>(android.R.id.text1).text.toString()
        assertEquals(context.getString(R.string.widget_error_fallback), text)
    }
}