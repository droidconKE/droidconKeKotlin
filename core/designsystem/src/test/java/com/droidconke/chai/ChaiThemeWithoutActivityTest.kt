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
package com.droidconke.chai

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies ChaiTheme renders in both themes without touching Activity/window APIs (closes B5).
 */
@RunWith(RobolectricTestRunner::class)
class ChaiThemeWithoutActivityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `chai theme renders light and dark without requiring an activity window`() {
        composeRule.setContent {
            ChaiTheme(darkTheme = false) {
                Text(text = "Light", color = MaterialTheme.colorScheme.onSurface)
            }
            ChaiTheme(darkTheme = true) {
                Text(text = "Dark", color = MaterialTheme.colorScheme.onSurface)
            }
        }

        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
    }
}