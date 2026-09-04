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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SharedElementModifiersTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shared modifiers are inert when no SharedTransitionLayout hosts them`() {
        composeTestRule.setContent {
            Column {
                Text(
                    text = "session-image",
                    modifier =
                        Modifier
                            .size(10.dp)
                            .sessionSharedImage("session-1", CircleShape)
                            .testTag("session-image"),
                )
                Text(
                    text = "session-title",
                    modifier =
                        Modifier
                            .size(10.dp)
                            .sessionSharedTitle("session-1")
                            .testTag("session-title"),
                )
                Text(
                    text = "speaker-image",
                    modifier =
                        Modifier
                            .size(10.dp)
                            .speakerSharedImage("Ada Lovelace", CircleShape)
                            .testTag("speaker-image"),
                )
                Text(
                    text = "speaker-name",
                    modifier =
                        Modifier
                            .size(10.dp)
                            .speakerSharedName("Ada Lovelace")
                            .testTag("speaker-name"),
                )
            }
        }

        composeTestRule.onNodeWithTag("session-image").assertIsDisplayed()
        composeTestRule.onNodeWithTag("session-title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("speaker-image").assertIsDisplayed()
        composeTestRule.onNodeWithTag("speaker-name").assertIsDisplayed()
    }
}