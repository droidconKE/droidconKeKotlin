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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.droidconke.chai.ChaiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SESSION_ID = "1"
private const val SPEAKER_NAME = "Ada"
private const val DETAIL_PANE_TAG = "detail_in_a_pane"
private const val DETAIL_FULL_TAG = "detail_full_screen"

private val compactWindow = DpSize(411.dp, 891.dp)
private val expandedWindow = DpSize(1280.dp, 900.dp)

private fun tagFor(screen: Screens): String = "pane_${screen::class.simpleName}"

/**
 * What the pane metadata and the scene strategies actually produce, at a phone width and at a
 * width with room for two panes.
 *
 * The screens are stand-ins; the metadata, the strategies and the back stack are the real ones,
 * because those are what decide whether a session opens beside its list or on top of it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ListDetailSceneTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `an expanded window shows a session beside its list`() {
        setContent(expandedWindow) { navController ->
            navController.navigate(Screens.Sessions)
            navController.navigate(Screens.SessionDetails(SESSION_ID))
        }

        composeTestRule.onNodeWithTag(tagFor(Screens.Sessions)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DETAIL_PANE_TAG).assertIsDisplayed()
    }

    @Test
    fun `a compact window shows the session instead of its list`() {
        setContent(compactWindow) { navController ->
            navController.navigate(Screens.Sessions)
            navController.navigate(Screens.SessionDetails(SESSION_ID))
        }

        composeTestRule.onNodeWithTag(DETAIL_FULL_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(tagFor(Screens.Sessions)).assertDoesNotExist()
    }

    @Test
    fun `a speaker opens beside the speakers list, not beside the sessions list`() {
        setContent(expandedWindow) { navController ->
            navController.navigate(Screens.Speakers)
            navController.navigate(Screens.SpeakerDetails(SPEAKER_NAME))
        }

        composeTestRule.onNodeWithTag(tagFor(Screens.Speakers)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DETAIL_PANE_TAG).assertIsDisplayed()
    }

    /**
     * The case the guard exists for. Home pushes a session detail with no list behind it, and
     * the Material strategy would still expand a second pane and fill it with nothing.
     */
    @Test
    fun `a session opened with no list behind it takes the whole window`() {
        setContent(expandedWindow) { navController ->
            navController.navigate(Screens.SessionDetails(SESSION_ID))
        }

        // Not merely "the detail is wide": the screens read the scene to decide whether to draw
        // a back arrow, and a full-screen detail that thinks it is a pane has no way back.
        composeTestRule.onNodeWithTag(DETAIL_FULL_TAG).assertIsDisplayed()

        val bounds = composeTestRule.onNodeWithTag(DETAIL_FULL_TAG).getUnclippedBoundsInRoot()
        assertEquals(
            "With no list to draw beside it the detail should fill the window, not sit in a " +
                "pane next to an empty column",
            expandedWindow.width.value,
            (bounds.right - bounds.left).value,
            1f,
        )
    }

    @Test
    fun `the happening now pane stands beside a top level destination`() {
        setContent(expandedWindow, supportingRoute = Screens.HappeningNow) { navController ->
            navController.navigate(Screens.Sessions)
        }

        composeTestRule.onNodeWithTag(tagFor(Screens.Sessions)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(tagFor(Screens.HappeningNow)).assertIsDisplayed()
    }

    @Test
    fun `the production entry provider marks each route with its pane role`() {
        var provider: ((NavKey) -> NavEntry<NavKey>)? = null
        composeTestRule.setContent {
            ChaiTheme {
                val navigationState = rememberNavigationState(Screens.Home, bottomNavigationSet)
                val navController = remember { NavigationController(navigationState) }
                provider = droidconEntryProvider(navController, onActionClicked = {})
            }
        }
        val entries = requireNotNull(provider)

        assertEquals(
            DroidconPaneScene.Sessions,
            entries(Screens.Sessions).metadata[LIST_SCENE_KEY],
        )
        assertEquals(
            DroidconPaneScene.Sessions,
            entries(Screens.SessionDetails(SESSION_ID)).metadata[DETAIL_SCENE_KEY],
        )
        assertEquals(
            DroidconPaneScene.Speakers,
            entries(Screens.Speakers).metadata[LIST_SCENE_KEY],
        )
        assertEquals(
            DroidconPaneScene.Speakers,
            entries(Screens.SpeakerDetails(SPEAKER_NAME)).metadata[DETAIL_SCENE_KEY],
        )

        // Key sets, so dropping a role from any entry shows up here rather than as a pane that
        // silently stops appearing on a tablet nobody tested on.
        assertEquals(
            listPaneMetadata(DroidconPaneScene.Sessions) {}.keys,
            entries(Screens.Sessions).metadata.keys,
        )
        assertEquals(
            detailPaneMetadata(DroidconPaneScene.Sessions).keys,
            entries(Screens.SessionDetails(SESSION_ID)).metadata.keys,
        )
        assertEquals(
            supportingPaneMetadata().keys,
            entries(Screens.HappeningNow).metadata.keys,
        )
        listOf(Screens.Home, Screens.Feed, Screens.About).forEach { route ->
            assertEquals(
                route.toString(),
                mainPaneMetadata().keys,
                entries(route).metadata.keys,
            )
        }
    }

    private fun setContent(
        windowSize: DpSize,
        supportingRoute: NavKey? = null,
        navigate: (NavigationController) -> Unit = {},
    ) {
        val controller = mutableStateOf<NavigationController?>(null)
        composeTestRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(windowSize)) {
                ChaiTheme {
                    val navigationState =
                        rememberNavigationState(Screens.Home, bottomNavigationSet)
                    val navController = remember { NavigationController(navigationState) }
                    controller.value = navController
                    Navigation(
                        navController = navController,
                        navigationState = navigationState,
                        supportingRoute = supportingRoute,
                        entryProvider = paneAwareEntryProvider(),
                    )
                }
            }
        }
        composeTestRule.runOnUiThread { navigate(requireNotNull(controller.value)) }
        composeTestRule.waitForIdle()
    }
}

/** The production pane metadata over stand-in screens. */
@Composable
private fun paneAwareEntryProvider(): (NavKey) -> NavEntry<NavKey> =
    entryProvider<NavKey> {
        entry<Screens.Home>(metadata = mainPaneMetadata()) { Stand(it) }
        entry<Screens.Feed>(metadata = mainPaneMetadata()) { Stand(it) }
        entry<Screens.About>(metadata = mainPaneMetadata()) { Stand(it) }
        entry<Screens.FeedBack> { Stand(it) }
        entry<Screens.Sessions>(
            metadata = listPaneMetadata(DroidconPaneScene.Sessions) { Text("no session") },
        ) { Stand(it) }
        entry<Screens.SessionDetails>(
            metadata = detailPaneMetadata(DroidconPaneScene.Sessions),
        ) { StandDetail(it) }
        entry<Screens.Speakers>(
            metadata = listPaneMetadata(DroidconPaneScene.Speakers) { Text("no speaker") },
        ) { Stand(it) }
        entry<Screens.SpeakerDetails>(
            metadata = detailPaneMetadata(DroidconPaneScene.Speakers),
        ) { StandDetail(it) }
        entry<Screens.HappeningNow>(metadata = supportingPaneMetadata()) { Stand(it) }
    }

@Composable
private fun Stand(screen: Screens) {
    val tag = tagFor(screen)
    Text(text = tag, modifier = Modifier.fillMaxSize().testTag(tag))
}

/**
 * A stand-in detail that reports what the real ones read.
 *
 * `SessionDetailsRoute` and `SpeakerDetailsRoute` drop their app bar and back arrow when
 * [LocalListDetailSceneScope] says a list is beside them, so getting that answer wrong is a
 * detail with no way back rather than a cosmetic difference.
 */
@Composable
private fun StandDetail(screen: Screens) {
    val tag = if (LocalListDetailSceneScope.current == null) DETAIL_FULL_TAG else DETAIL_PANE_TAG
    Text(text = tagFor(screen), modifier = Modifier.fillMaxSize().testTag(tag))
}