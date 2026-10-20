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
package com.android254.presentation.feed.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android254.domain.models.Feed
import com.android254.domain.models.ScheduleUpdate
import com.android254.domain.repos.FeedRepo
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.UpdatesRepo
import com.android254.presentation.feed.FeedViewModel
import com.android254.presentation.models.FeedUI
import com.droidconke.chai.ChaiTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class FeedScreenTest {
    private val repo = mockk<FeedRepo>()
    private val updatesRepo = mockk<UpdatesRepo> { every { scheduleUpdates() } returns flowOf(emptyList()) }
    private val sessionsRepo = mockk<SessionsRepo> { every { fetchSessions() } returns flowOf(emptyList()) }
    private val clock =
        object : Clock {
            override fun now() = Instant.parse("2026-10-20T09:00:00+03:00")
        }
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun viewModel() = FeedViewModel(repo, updatesRepo, sessionsRepo, clock, TimeZone.of("Africa/Nairobi"), testDispatcher)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `should display feed items`() {
        coEvery { repo.fetchFeed() } returns
            flowOf(
                listOf(
                    Feed(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                    ),
                ),
            )

        val viewModel = viewModel()
        composeTestRule.setContent {
            ChaiTheme {
                FeedRoute(feedViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("feeds_lazy_column").assertExists()
        composeTestRule.onNodeWithTag("feeds_lazy_column").assertIsDisplayed()

        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertIsDisplayed()
    }

    @Test
    fun `tapping share hands that post to the share sheet`() {
        val post = FeedUI(title = "Venue announced", body = "Back at Sarit", topic = "", url = "https://droidcon.co.ke", image = null, createdAt = "")
        var shared: FeedUI? = null
        composeTestRule.setContent {
            ChaiTheme {
                FeedScreen(feedUIState = FeedUIState.Success(persistentListOf(UpdateItem.Post(post, atMillis = null))), onShare = { shared = it })
            }
        }

        composeTestRule.onNodeWithTag("share_button").performClick()
        assertEquals(post, shared)
    }

    @Test
    fun `renders feed items that share a url`() {
        coEvery { repo.fetchFeed() } returns
            flowOf(
                listOf(
                    Feed("Call for speakers", "", "", "", "", ""),
                    Feed("Venue announced", "", "", "", "", ""),
                ),
            )

        val viewModel = viewModel()
        composeTestRule.setContent {
            ChaiTheme {
                FeedRoute(feedViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("feeds_lazy_column").assertIsDisplayed()
    }

    @Test
    fun `two sessions dropped in the same sync both show`() {
        composeTestRule.setContent {
            ChaiTheme {
                FeedScreen(
                    feedUIState =
                        FeedUIState.Success(
                            persistentListOf(
                                schedule(ScheduleUpdate.Kind.Removed, sessionId = null, title = "Old talk", id = 1),
                                schedule(ScheduleUpdate.Kind.Removed, sessionId = null, title = "Older talk", id = 2),
                            ),
                        ),
                )
            }
        }

        composeTestRule.onNodeWithText("Old talk").assertIsDisplayed()
        composeTestRule.onNodeWithText("Older talk").assertIsDisplayed()
    }

    @Test
    fun `a schedule change opens its session, and the schedule coming out opens sessions`() {
        val moved = schedule(ScheduleUpdate.Kind.Moved, sessionId = "kotlin", title = "Kotlin Everywhere", id = 1)
        val out = schedule(ScheduleUpdate.Kind.ScheduleOut, sessionId = null, title = "", count = 20, id = 2)
        var opened: String? = null
        var browsed = false
        composeTestRule.setContent {
            ChaiTheme {
                FeedScreen(
                    feedUIState = FeedUIState.Success(persistentListOf(moved, out)),
                    onSessionClick = { opened = it },
                    navigateToSessions = { browsed = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Kotlin Everywhere").performClick()
        composeTestRule.onNodeWithText("The schedule is out").performClick()
        assertEquals("kotlin", opened)
        assertTrue(browsed)
    }

    private fun schedule(
        kind: ScheduleUpdate.Kind,
        sessionId: String?,
        title: String,
        count: Int = 0,
        id: Long = 1,
    ) = UpdateItem.Schedule(id, kind, sessionId, title, count, "Today", "Thu, 5 Nov · 10:15 AM", "Opal", isStarred = false, atMillis = 1)
}