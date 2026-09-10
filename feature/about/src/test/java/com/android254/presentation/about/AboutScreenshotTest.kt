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
package com.android254.presentation.about

import com.android254.presentation.about.view.AboutScreen
import com.android254.presentation.about.view.AboutScreenUiState
import com.android254.presentation.feedback.view.FeedBackScreen
import com.android254.presentation.models.OrganizingTeamMember
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class AboutScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun about() =
        captureScreen("screens/about") {
            AboutScreen(
                uiState =
                    AboutScreenUiState.Success(
                        teamMembers =
                            persistentListOf(
                                OrganizingTeamMember(
                                    name = "Member One",
                                    desc = "Organiser",
                                    image = "",
                                ),
                                OrganizingTeamMember(
                                    name = "Member Two",
                                    desc = "Organiser",
                                    image = "",
                                ),
                            ),
                        stakeHoldersLogos = emptyList(),
                    ),
            )
        }

    @Test
    fun feedback() =
        captureScreen("screens/feedback") {
            FeedBackScreen(darkTheme = false)
        }
}