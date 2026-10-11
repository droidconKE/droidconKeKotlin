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
package com.android254.presentation.about.view

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.android254.presentation.common.components.OrganizedBySection
import com.android254.presentation.models.OrganizerLogo
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OrganizedBySectionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `each organiser logo is named for TalkBack`() {
        composeTestRule.setContent {
            ChaiTheme {
                OrganizedBySection(
                    organizers =
                        persistentListOf(
                            OrganizerLogo(name = "Android254", logo = "https://example.com/a.png"),
                            OrganizerLogo(name = "Kotlin Kenya", logo = "https://example.com/k.png"),
                        ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Android254").assertExists()
        composeTestRule.onNodeWithContentDescription("Kotlin Kenya").assertExists()
    }
}