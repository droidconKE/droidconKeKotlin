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
package com.android254.presentation.home.mappers

import com.android254.domain.models.Home
import com.android254.domain.models.HomeBanner
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.domain.models.Sponsors
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DomainToPresentationMappersTest {

    @Test
    fun `test Home to HomeState mapping`() {
        val speakers = listOf(
            Speaker(name = "Speaker 1", avatar = "avatar1", tagline = "tagline1", twitter = "twitter1")
        )
        val sponsors = listOf(
            Sponsors(sponsorName = "Sponsor 1", sponsorLogoUrl = "logo1", link = "link1", sponsorType = "Gold", createdAt = "")
        )
        val sessions = listOf(
            Session(
                id = "1",
                title = "Session 1",
                description = "Desc 1",
                rooms = "Room 1",
                startDateTime = "2023-11-16 10:00:00",
                endDateTime = "2023-11-16 11:00:00",
                isBookmarked = false,
                isKeynote = false,
                isServiceSession = false,
                sessionImage = null,
                speakers = emptyList(),
                remoteId = "remote1",
                sessionFormat = "Format 1",
                sessionLevel = "Beginner",
                slug = "slug1",
                startTime = "10:00",
                endTime = "11:00",
                eventDay = "1"
            )
        )
        val home = Home(
            banner = HomeBanner.EventPoster(link = "poster_link"),
            speakers = speakers,
            sponsors = sponsors,
            organizerLogos = listOf("logo1", "logo2"),
            sessions = sessions
        )

        val homeState = home.toHomeState(isSyncing = false)

        assertThat(homeState.banner, `is`(home.banner))
        assertThat(homeState.speakers.size, `is`(1))
        assertThat(homeState.speakers[0].name, `is`("Speaker 1"))
        assertThat(homeState.sponsors.size, `is`(1))
        assertThat(homeState.sponsors[0].name, `is`("Sponsor 1"))
        assertThat(homeState.organizerLogos.size, `is`(2))
        assertThat(homeState.sessions.size, `is`(1))
        assertThat(homeState.sessions[0].title, `is`("Session 1"))
        assertThat(homeState.isSyncing, `is`(false))
    }

    @Test
    fun `test Speaker to SpeakerUI mapping`() {
        val speakers = listOf(
            Speaker(name = "Speaker 1", avatar = "avatar1", tagline = "tagline1", twitter = "twitter1", biography = "bio1")
        )
        val speakersUI = speakers.toSpeakersPresentation()

        assertThat(speakersUI.size, `is`(1))
        assertThat(speakersUI[0].name, `is`("Speaker 1"))
        assertThat(speakersUI[0].imageUrl, `is`("avatar1"))
        assertThat(speakersUI[0].tagline, `is`("tagline1"))
        assertThat(speakersUI[0].twitterHandle, `is`("twitter1"))
        assertThat(speakersUI[0].bio, `is`("bio1"))
    }

    @Test
    fun `test Sponsors to SponsorPresentationModel mapping`() {
        val sponsor = Sponsors(sponsorName = "Sponsor 1", sponsorLogoUrl = "logo1", link = "link1", sponsorType = "Gold", createdAt = "")
        val sponsorUI = sponsor.toPresentation()

        assertThat(sponsorUI.name, `is`("Sponsor 1"))
        assertThat(sponsorUI.logo, `is`("logo1"))
        assertThat(sponsorUI.link, `is`("link1"))
        assertThat(sponsorUI.sponsorType, `is`("Gold"))
    }
}
