/*
 * Copyright 2022 DroidconKE
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
package com.android254.presentation.home.screen

import androidx.activity.FullyDrawnReporter
import androidx.activity.FullyDrawnReporterOwner
import androidx.activity.compose.LocalFullyDrawnReporterOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android254.presentation.common.components.SponsorsCard
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.home.components.HomeHeaderSectionComponent
import com.android254.presentation.home.components.HomeSessionSection
import com.android254.presentation.home.components.HomeSpeakersSection
import com.android254.presentation.home.components.HomeToolbarComponent
import com.android254.presentation.home.viewstate.HomeState
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.models.SponsorPresentationModel
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.Executor

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `Test home title is displayed`() {
        composeTestRule.setContent {
            ChaiTheme {
                HomeHeaderSectionComponent()
            }
        }

        composeTestRule.onNodeWithTag("home_header").assertIsDisplayed()
    }

    @Test
    fun `Test speakers view is displayed`() {
        composeTestRule.setContent {
            ChaiTheme {
                HomeSpeakersSection(speakers = persistentListOf())
            }
        }

        composeTestRule.onNodeWithTag("sectionHeader").assertIsDisplayed()
        composeTestRule.onNodeWithTag("speakersRow").assertExists()
        composeTestRule.onNodeWithTag("viewAll").assertExists()
    }

    @Test
    fun `Not signedIn droidcon topBar is displayed`() {
        composeTestRule.setContent {
            ChaiTheme {
                HomeToolbarComponent(isSignedIn = false)
            }
        }

        composeTestRule.onNodeWithTag("droidcon_topBar_notSignedIn").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_notSignedIn").assertIsDisplayed()
    }

    @Test
    fun `SignedIn droidcon topBar is displayed`() {
        composeTestRule.setContent {
            ChaiTheme {
                HomeToolbarComponent(isSignedIn = true)
            }
        }

        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertIsDisplayed()
    }

    @Test
    fun `Test sponsors card is displayed`() {
        composeTestRule.setContent {
            ChaiTheme {
                SponsorsCard(sponsors = persistentListOf(SponsorPresentationModel("", "", "", "")))
            }
        }
        composeTestRule.onNodeWithTag("sponsors_section").assertIsDisplayed()
    }

    @Test
    fun `Test sessions is displayed`() {
        composeTestRule.setContent {
            ChaiTheme {
                HomeSessionSection(
                    sessions = persistentListOf(),
                    onViewAllSessionClicked = {},
                    onSessionClick = {},
                )
            }
        }
        composeTestRule.onNodeWithTag("sectionHeader").assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewAll").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sessions").assertExists()
    }

    @Test
    fun `Test HomeScreen displays correctly with HomeState`() {
        composeTestRule.setContent {
            ChaiTheme {
                HomeScreen(
                    viewState = HomeState(),
                    isSyncing = false,
                )
            }
        }

        composeTestRule.onNodeWithTag("home_header").assertIsDisplayed()
        // Sponsors card is hidden because sponsors list is empty (AnimatedVisibility)
        composeTestRule.onNodeWithTag("sponsors_section").assertDoesNotExist()
    }

    @Test
    fun `HomeScreen reports fully drawn once content is on screen`() {
        val owner = RecordingFullyDrawnReporterOwner()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFullyDrawnReporterOwner provides owner) {
                ChaiTheme {
                    HomeScreen(
                        viewState =
                            HomeState(
                                sessions = fakeSessions.take(1).toImmutableList(),
                                speakers = persistentListOf(SpeakerUI(id = 1, name = "Speaker")),
                            ),
                        isSyncing = false,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        assertTrue(owner.fullyDrawnReporter.isFullyDrawnReported)
    }

    @Test
    fun `HomeScreen does not report fully drawn while syncing`() {
        val owner = RecordingFullyDrawnReporterOwner()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFullyDrawnReporterOwner provides owner) {
                ChaiTheme {
                    HomeScreen(
                        viewState = HomeState(speakers = persistentListOf(SpeakerUI(id = 1, name = "Speaker"))),
                        isSyncing = true,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        assertFalse(owner.fullyDrawnReporter.isFullyDrawnReported)
    }

    @Test
    fun `HomeScreen does not report fully drawn while a section is still empty`() {
        val owner = RecordingFullyDrawnReporterOwner()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalFullyDrawnReporterOwner provides owner) {
                ChaiTheme {
                    HomeScreen(
                        viewState = HomeState(speakers = persistentListOf(SpeakerUI(id = 1, name = "Speaker"))),
                        isSyncing = false,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        assertFalse(owner.fullyDrawnReporter.isFullyDrawnReported)
    }
}

private class RecordingFullyDrawnReporterOwner : FullyDrawnReporterOwner {
    override val fullyDrawnReporter = FullyDrawnReporter(Executor { it.run() }) {}
}