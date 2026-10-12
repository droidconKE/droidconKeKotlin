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
package com.android254.presentation.sessions.view

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.models.EventDate
import com.android254.presentation.sessions.models.SessionClash
import com.android254.presentation.sessions.models.SessionsUiState
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClashBannerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `with My sessions on, the banner names the clash and announces it politely`() {
        show(showMySessionsOnly = true)

        composeTestRule
            .onNodeWithTag("clash_banner")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
            .assert(hasText("“Compose at scale” and “Kotlin everywhere”"))
    }

    @Test
    fun `with My sessions off, there is no banner over the full list`() {
        show(showMySessionsOnly = false)

        composeTestRule.onNodeWithTag("clash_banner").assertDoesNotExist()
    }

    private fun show(showMySessionsOnly: Boolean) {
        composeTestRule.setContent {
            ChaiTheme {
                SessionsScreen(
                    sessionsUiState =
                        SessionsUiState(
                            eventDays = persistentListOf(EventDate("06", 1)),
                            sessionStatus = ResultStatus.Success,
                            showMySessionsOnly = showMySessionsOnly,
                            clashes = persistentListOf(SessionClash("Compose at scale", "Kotlin everywhere")),
                        ),
                    isRefreshing = false,
                    selectedEventDate = EventDate("06", 1),
                    currentSelections = persistentListOf(),
                    navigateToSessionDetails = {},
                    onEvent = {},
                )
            }
        }
    }
}