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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android254.domain.models.Feed
import com.android254.domain.repos.FeedRepo
import com.android254.presentation.common.theme.DroidconKE2023Theme
import com.android254.presentation.feed.FeedViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class FeedScreenTest {

    private val repo = mockk<FeedRepo>()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `should display feed items`() {
        coEvery { repo.fetchFeed() } returns flowOf(
            listOf(
                Feed(
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                )
            )
        )

        composeTestRule.setContent {
            DroidconKE2023Theme {
                FeedScreen(feedViewModel = FeedViewModel(repo))
            }
        }

        composeTestRule.onNodeWithTag("feeds_lazy_column").assertExists()
        composeTestRule.onNodeWithTag("feeds_lazy_column").assertIsDisplayed()

        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertIsDisplayed()
    }

    @Test
    fun `test share bottom sheet is shown`() {
        coEvery { repo.fetchFeed() } returns flowOf(
            listOf(
                Feed(
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                )
            )
        )

        composeTestRule.setContent {
            DroidconKE2023Theme {
                FeedScreen(feedViewModel = FeedViewModel(repo))
            }
        }
        composeTestRule.onNodeWithTag("share_button").assertExists()
        composeTestRule.onNodeWithTag("share_button").performClick()
        composeTestRule.onNodeWithTag("share_bottom_sheet").assertIsDisplayed()
    }
}