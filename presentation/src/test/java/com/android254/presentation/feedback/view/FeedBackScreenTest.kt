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
package com.android254.presentation.feedback.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.android254.presentation.common.theme.DroidconKE2022Theme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class FeedBackScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun `should should show FeedBack Screen and show the heading`() {
        composeTestRule.setContent {
            DroidconKE2022Theme {
                FeedBackScreen()
            }
        }

        composeTestRule.onNodeWithTag("heading").assertIsDisplayed()
        composeTestRule.onNodeWithTag("heading").assertTextEquals("Feedback")
    }

    @Test
    fun `should show submit Button`() {
        composeTestRule.setContent {
            DroidconKE2022Theme {
                FeedBackScreen()
            }
        }

        composeTestRule.onNodeWithTag("submit_feedback_button").performScrollTo().assertIsDisplayed()
    }
}