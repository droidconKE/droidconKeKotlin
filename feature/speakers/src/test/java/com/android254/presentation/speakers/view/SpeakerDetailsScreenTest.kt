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
package com.android254.presentation.speakers.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import com.android254.presentation.speakers.SpeakerDetailsScreenViewModel
import com.droidconke.chai.ChaiTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SpeakerDetailsScreenTest {
    private val speakersRepo = mockk<SpeakersRepo>()
    private val sessionsRepo = mockk<SessionsRepo>()
    private val testDispatcher = UnconfinedTestDispatcher()

    /** Fixed so session status never depends on when the suite runs. */
    private val fixedClock =
        object : Clock {
            override fun now(): Instant = Instant.parse("2026-08-27T06:45:00Z")
        }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val speaker =
        Speaker(
            name = "John Doe",
            tagline = "some tag line",
            biography = "a short bio",
            twitter = "https://twitter.com/johndoe",
        )

    private fun session(
        title: String,
        speakers: List<Speaker>,
    ) = Session(
        id = title,
        endDateTime = "2026-08-27 10:15:00",
        endTime = "10:15",
        isBookmarked = false,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = null,
        startDateTime = "2026-08-27 09:30:00",
        startTime = "09:30",
        rooms = "Main Hall",
        speakers = speakers,
        remoteId = "remote-$title",
        description = "a session",
        sessionFormat = "Session",
        sessionLevel = "Beginner",
        slug = title,
        title = title,
        eventDay = "2026-08-27",
    )

    private fun viewModel(sessions: List<Session> = emptyList()): SpeakerDetailsScreenViewModel {
        coEvery { speakersRepo.getSpeakerByName("Harun Wangereka") } returns flowOf(speaker)
        every { sessionsRepo.fetchSessions() } returns flowOf(sessions)
        return SpeakerDetailsScreenViewModel(
            speakersRepo = speakersRepo,
            sessionsRepo = sessionsRepo,
            clock = fixedClock,
            ioDispatcher = testDispatcher,
        )
    }

    @Test
    fun `all components should be displayed properly`() {
        composeTestRule.setContent {
            ChaiTheme {
                SpeakerDetailsRoute(
                    name = "Harun Wangereka",
                    speakersDetailsScreenViewModel = viewModel(),
                )
            }
        }

        with(composeTestRule) {
            onNodeWithTag("speaker_name").assertIsDisplayed()
            onNodeWithTag("speaker_tagline").assertIsDisplayed()
            onNodeWithTag("speaker_image").assertIsDisplayed()
            onNodeWithTag("speaker_bio").performScrollTo().assertIsDisplayed()
            onNodeWithTag("twitter_button").performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun `should show sessions the speaker is presenting`() {
        val sessions = listOf(session("Android 254", listOf(speaker)))
        composeTestRule.setContent {
            ChaiTheme {
                SpeakerDetailsRoute(
                    name = "Harun Wangereka",
                    speakersDetailsScreenViewModel = viewModel(sessions),
                )
            }
        }

        with(composeTestRule) {
            onNodeWithTag("speaker_session_Android 254").performScrollTo().assertIsDisplayed()
            onNodeWithText("Android 254").performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun `should not show sessions belonging to other speakers`() {
        val sessions =
            listOf(
                session("Android 254", listOf(speaker)),
                session("Someone Else's Talk", listOf(Speaker(name = "Jane Smith"))),
            )
        composeTestRule.setContent {
            ChaiTheme {
                SpeakerDetailsRoute(
                    name = "Harun Wangereka",
                    speakersDetailsScreenViewModel = viewModel(sessions),
                )
            }
        }

        with(composeTestRule) {
            onNodeWithTag("speaker_session_Android 254").performScrollTo().assertIsDisplayed()
            onNodeWithTag("speaker_session_Someone Else's Talk").assertDoesNotExist()
        }
    }
}