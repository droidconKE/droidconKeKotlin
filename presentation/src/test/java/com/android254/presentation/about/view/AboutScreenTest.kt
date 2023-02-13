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
package com.android254.presentation.about.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android254.presentation.common.theme.DroidconKE2022Theme
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class AboutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    @Ignore("Fix later")
    fun `should show About Screen and organizing team section`() {
        composeTestRule.setContent {
            DroidconKE2022Theme {
                AboutScreen()
            }
        }

        composeTestRule.onNodeWithTag("about_screen").assertExists()
        composeTestRule.onNodeWithTag("about_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("organizing_team_section").assertExists()
    }

    @Test
    @Ignore("Fix later")
    fun `should show About Screen and the droidcon topBar`() {
        composeTestRule.setContent {
            DroidconKE2022Theme {
                AboutScreen()
            }
        }

        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertExists()
        composeTestRule.onNodeWithTag("droidcon_topBar_with_Feedback").assertIsDisplayed()
    }

    @Test
    @Ignore("Fix later")
    fun `should show About Screen and show the organized by section`() {
        composeTestRule.setContent {
            DroidconKE2022Theme {
                AboutScreen()
            }
        }

        composeTestRule.onNodeWithTag("organized_by_section").assertExists()
    }
}