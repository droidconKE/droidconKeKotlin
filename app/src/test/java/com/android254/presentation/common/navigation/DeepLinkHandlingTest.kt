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
package com.android254.presentation.common.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.android254.presentation.activity.MainActivity
import com.droidconke.chai.ChaiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class DeepLinkHandlingTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()

    private fun view(link: String) = Intent(Intent.ACTION_VIEW, Uri.parse(link)).setPackage(context.packageName)

    @Test
    fun `the manifest delivers the app's links to the main activity`() {
        listOf(DeepLinks.session("s1"), DeepLinks.feed(), "droidconke://feed/p1").forEach { link ->
            val resolved = context.packageManager.queryIntentActivities(view(link), 0)
            assertEquals(link, listOf(MainActivity::class.java.name), resolved.map { it.activityInfo.name })
        }
    }

    @Test
    fun `a view intent carries its destination`() {
        assertEquals(Screens.SessionDetails("s1"), deepLinkFrom(view(DeepLinks.session("s1"))))
    }

    @Test
    fun `a launcher intent carries no destination`() {
        assertNull(deepLinkFrom(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)))
    }

    @Test
    fun `reopening from recents does not replay the link`() {
        val fromHistory = view(DeepLinks.session("s1")).addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)

        assertNull(deepLinkFrom(fromHistory))
    }

    @Test
    fun `a pending deep link opens its destination once`() {
        var pending: Screens? by mutableStateOf(Screens.SessionDetails("s1"))
        var handledCount = 0
        lateinit var state: NavigationState

        composeTestRule.setContent {
            ChaiTheme {
                state = rememberNavigationState(startRoute = Screens.Home, topLevelRoutes = bottomNavigationSet)
                val navController = remember { NavigationController(state) }
                DeepLinkEffect(
                    deepLink = pending,
                    navController = navController,
                    onHandled = {
                        handledCount++
                        pending = null
                    },
                )
                Navigation(navController = navController, navigationState = state, entryProvider = fakeEntryProvider())
            }
        }

        composeTestRule.onNodeWithText("Argument (Session ID): s1").assertExists()
        assertEquals(1, handledCount)
        assertEquals(listOf(Screens.Home, Screens.SessionDetails("s1")), state.backStacks.getValue(Screens.Home).toList())
    }

    @Test
    fun `a feed link switches to the feed tab`() {
        lateinit var state: NavigationState

        composeTestRule.setContent {
            ChaiTheme {
                state = rememberNavigationState(startRoute = Screens.Home, topLevelRoutes = bottomNavigationSet)
                val navController = remember { NavigationController(state) }
                DeepLinkEffect(deepLink = Screens.Feed, navController = navController, onHandled = {})
                Navigation(navController = navController, navigationState = state, entryProvider = fakeEntryProvider())
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(state.topLevelRoute == Screens.Feed)
    }
}