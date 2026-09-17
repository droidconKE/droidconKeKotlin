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

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.droidconke.chai.ChaiTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What the system back button does, at the sizes where the answer changes.
 *
 * `NavDisplay` decides whether to intercept back from the entries it is handed, and at expanded
 * widths those include the standing "happening now" pane. So on a tablet it intercepts even on
 * the start destination, where the back stack has nothing to pop — and a back press that is
 * intercepted and then quietly dropped is an app you cannot leave.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackHandlingTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `back leaves the app from the start destination beside a supporting pane`() {
        setContent(expandedWindow, supportingRoute = Screens.HappeningNow)

        pressBack()

        assertTrue(
            "Back on the landing screen must reach the system. NavDisplay intercepts it because " +
                "the supporting pane is in the entry list, so the fall-through is the only thing " +
                "standing between a tablet user and an app they cannot back out of",
            composeTestRule.activity.isFinishing,
        )
    }

    @Test
    fun `back leaves the app from the start destination on a phone`() {
        setContent(compactWindow)

        pressBack()

        assertTrue(composeTestRule.activity.isFinishing)
    }

    @Test
    fun `back on another tab returns to the start destination rather than leaving`() {
        val navController = setContent(expandedWindow, supportingRoute = Screens.HappeningNow)
        composeTestRule.runOnUiThread { navController.navigate(Screens.Sessions) }
        composeTestRule.waitForIdle()

        pressBack()

        assertFalse(
            "There was somewhere to go back to, so the app must not exit",
            composeTestRule.activity.isFinishing,
        )
        composeTestRule.onNodeWithTag(tag(Screens.Home)).assertExists()
    }

    /**
     * The Material default pops until the scaffold value changes, which here would carry on
     * past the list and out of the tab, because list-beside-placeholder and list-beside-detail
     * are the same scaffold value.
     */
    @Test
    fun `back out of a detail returns to its list rather than leaving`() {
        val navController = setContent(expandedWindow, supportingRoute = null)
        composeTestRule.runOnUiThread {
            navController.navigate(Screens.Sessions)
            navController.navigate(Screens.SessionDetails("1"))
        }
        composeTestRule.waitForIdle()

        pressBack()

        assertFalse(composeTestRule.activity.isFinishing)
        composeTestRule.onNodeWithTag(tag(Screens.Sessions)).assertExists()
    }

    private fun pressBack() {
        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
    }

    private fun setContent(
        windowSize: DpSize,
        supportingRoute: NavKey? = null,
    ): NavigationController {
        val controller = mutableStateOf<NavigationController?>(null)
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(windowSize)) {
                ChaiTheme {
                    val navigationState =
                        rememberNavigationState(Screens.Home, bottomNavigationSet)
                    val navController = remember { NavigationController(navigationState) }
                    val activity = composeTestRule.activity
                    controller.value = navController
                    Navigation(
                        navController = navController,
                        navigationState = navigationState,
                        supportingRoute = supportingRoute,
                        // The wiring under test, as MainScreen does it.
                        onBack = { if (!navController.goBack()) activity.finish() },
                        entryProvider = backTestEntryProvider(),
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        return requireNotNull(controller.value)
    }
}

private val compactWindow = DpSize(411.dp, 891.dp)
private val expandedWindow = DpSize(1280.dp, 900.dp)

private fun tag(screen: Screens): String = "back_${screen::class.simpleName}"

/** The production pane metadata over stand-in screens. */
@Composable
private fun backTestEntryProvider(): (NavKey) -> NavEntry<NavKey> =
    entryProvider<NavKey> {
        entry<Screens.Home>(metadata = mainPaneMetadata()) { Stand(it) }
        entry<Screens.Feed>(metadata = mainPaneMetadata()) { Stand(it) }
        entry<Screens.About>(metadata = mainPaneMetadata()) { Stand(it) }
        entry<Screens.FeedBack> { Stand(it) }
        entry<Screens.Speakers>(
            metadata = listPaneMetadata(DroidconPaneScene.Speakers) { Text("no speaker") },
        ) { Stand(it) }
        entry<Screens.SpeakerDetails>(
            metadata = detailPaneMetadata(DroidconPaneScene.Speakers),
        ) { Stand(it) }
        entry<Screens.Sessions>(
            metadata = listPaneMetadata(DroidconPaneScene.Sessions) { Text("no session") },
        ) { Stand(it) }
        entry<Screens.SessionDetails>(
            metadata = detailPaneMetadata(DroidconPaneScene.Sessions),
        ) { Stand(it) }
        entry<Screens.HappeningNow>(metadata = supportingPaneMetadata()) { Stand(it) }
    }

@Composable
private fun Stand(screen: Screens) {
    Text(text = tag(screen), modifier = Modifier.fillMaxSize().testTag(tag(screen)))
}