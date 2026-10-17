/*
 * Copyright 2023 DroidconKE
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
package com.android254.presentation.notifications

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SystemNotificationSettingsTest {
    private val context: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        // Throw ActivityNotFoundException for intents nothing handles, as a real device does.
        shadowOf(context).checkActivities(true)
    }

    private fun handle(action: String) {
        val component = ComponentName("com.android.settings", "Settings$${action.substringAfterLast('.')}")
        shadowOf(context.packageManager).apply {
            addActivityIfNotPresent(component)
            addIntentFilterForActivity(
                component,
                IntentFilter(action).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    if (action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) addDataScheme("package")
                },
            )
        }
    }

    @Test
    fun `opens the app's notification settings`() {
        handle(Settings.ACTION_APP_NOTIFICATION_SETTINGS)

        openSystemNotificationSettings(context)

        val started = shadowOf(context).nextStartedActivity
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, started.action)
        assertEquals(context.packageName, started.getStringExtra(Settings.EXTRA_APP_PACKAGE))
    }

    @Test
    fun `a device without that screen falls back to the app's details`() {
        handle(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

        openSystemNotificationSettings(context)

        val started = shadowOf(context).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, started.action)
        assertEquals("package:${context.packageName}", started.dataString)
    }

    @Test
    fun `a device with neither screen does not crash`() {
        openSystemNotificationSettings(context)

        assertNull(shadowOf(context).nextStartedActivity)
    }
}