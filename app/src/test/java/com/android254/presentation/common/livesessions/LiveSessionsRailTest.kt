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
package com.android254.presentation.common.livesessions

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.models.SessionStatus
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LiveSessionsRailTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `a screen reader hears when the session on the rail changes`() {
        composeTestRule.setContent {
            ChaiTheme {
                LiveSessionsRail(
                    sessions = persistentListOf(fakeSessions[0].copy(sessionStatus = SessionStatus.Ongoing)),
                    onSessionClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(LIVE_SESSIONS_RAIL_TEST_TAG)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }
}