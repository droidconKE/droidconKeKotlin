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
package com.android254.presentation.sessions.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.android254.presentation.common.components.BookmarkButton
import com.droidconke.chai.ChaiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BookmarkButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun stateIs(text: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, text)

    @Test
    fun `a tap stars the session and announces it`() {
        composeTestRule.setContent {
            ChaiTheme {
                var starred by remember { mutableStateOf(false) }
                BookmarkButton(isBookmarked = starred, onToggle = { starred = !starred })
            }
        }
        val button = composeTestRule.onNodeWithContentDescription("Star session")

        button.assertIsOff().assert(stateIs("Not in my sessions"))
        button.performClick()
        button.assertIsOn().assert(stateIs("In my sessions"))
    }

    @Test
    fun `the touch target is at least 48 dp`() {
        composeTestRule.setContent {
            ChaiTheme { BookmarkButton(isBookmarked = false, onToggle = {}) }
        }

        composeTestRule
            .onNodeWithContentDescription("Star session")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }
}