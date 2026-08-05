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
package com.android254.presentation.common.fakedata

import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionSpeakersPresentationModel
import com.android254.presentation.models.SessionStatus

private const val SAPPHIRE_OPAL = "Sapphire,Opal"
private const val SAPPHIRE = "Sapphire"
private const val OPAL = "Opal"
private const val DATE_2026_08_14 = "2026-08-14"
private const val DATE_2026_08_15 = "2026-08-15"
private const val TIME_11_00 = "11:00"
private const val LEVEL_INTERMEDIATE = "Intermediate"

val fakeSessions =
    listOf(
        SessionPresentationModel(
            id = "1",
            title = "Opening Keynote: Building for Africa",
            description = "The future of Android development across Africa.",
            venue = SAPPHIRE_OPAL,
            startTime = "09:00",
            endTime = "10:00",
            amOrPm = "AM",
            isStarred = true,
            format = "Keynote",
            level = "All Levels",
            sessionStatus = SessionStatus.Past,
            startDate = DATE_2026_08_14,
            endDate = DATE_2026_08_14,
            remoteId = "remote_1",
            eventDay = "14",
            speakers =
                listOf(
                    SessionSpeakersPresentationModel(
                        name = "Florence Mwangangi",
                        speakerImage = "",
                        twitterHandle = "florencedev",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "2",
            title = "Compose Animations That Don’t Jank",
            description = "Practical animation performance tips in Compose.",
            venue = SAPPHIRE,
            startTime = "10:15",
            endTime = TIME_11_00,
            amOrPm = "AM",
            isStarred = false,
            sessionStatus = SessionStatus.Past,
            format = "Talk",
            level = LEVEL_INTERMEDIATE,
            startDate = DATE_2026_08_14,
            endDate = DATE_2026_08_14,
            remoteId = "remote_2",
            eventDay = "14",
            speakers =
                listOf(
                    SessionSpeakersPresentationModel(
                        name = "Donald Okara",
                        speakerImage = "",
                        twitterHandle = "donnycodes",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "3",
            title = "Coffee Break",
            description = "Grab coffee and network with attendees.",
            venue = OPAL,
            startTime = TIME_11_00,
            endTime = "11:30",
            amOrPm = "AM",
            isStarred = false,
            sessionStatus = SessionStatus.Past,
            format = "Break",
            level = "",
            startDate = DATE_2026_08_14,
            endDate = DATE_2026_08_14,
            remoteId = "remote_3",
            isService = true,
            eventDay = "14",
            speakers = emptyList(),
        ),
        SessionPresentationModel(
            id = "4",
            title = "Kotlin Multiplatform in Production",
            description = "Lessons from shipping KMP apps at scale.",
            venue = SAPPHIRE,
            startTime = "11:30",
            endTime = "12:15",
            amOrPm = "AM",
            isStarred = true,
            sessionStatus = SessionStatus.Ongoing,
            format = "Session",
            level = "Advanced",
            startDate = DATE_2026_08_14,
            endDate = DATE_2026_08_14,
            remoteId = "remote_4",
            eventDay = "14",
            speakers =
                listOf(
                    SessionSpeakersPresentationModel(
                        name = "Brian Kimani",
                        speakerImage = "",
                        twitterHandle = "briankimani",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "5",
            title = "Lunch Break",
            description = "Lunch served at the networking hall.",
            venue = SAPPHIRE_OPAL,
            startTime = "12:15",
            endTime = "01:30",
            amOrPm = "PM",
            isStarred = false,
            format = "Break",
            level = "",
            sessionStatus = SessionStatus.Ongoing,
            startDate = DATE_2026_08_14,
            endDate = DATE_2026_08_14,
            remoteId = "remote_5",
            isService = true,
            eventDay = "14",
            speakers = emptyList(),
        ),
        SessionPresentationModel(
            id = "6",
            title = "State Management in Compose",
            description = "Handling complex UI state without pain.",
            venue = OPAL,
            startTime = "01:30",
            endTime = "02:15",
            amOrPm = "PM",
            sessionStatus = SessionStatus.Upcoming,
            isStarred = false,
            format = "Talk",
            level = "Beginner",
            startDate = DATE_2026_08_14,
            endDate = DATE_2026_08_14,
            remoteId = "remote_6",
            eventDay = "14",
            speakers =
                listOf(
                    SessionSpeakersPresentationModel(
                        name = "Amina Yusuf",
                        speakerImage = "",
                        twitterHandle = "aminadev",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "7",
            title = "Workshop: Advanced Coroutines",
            description = "Deep dive into structured concurrency.",
            venue = SAPPHIRE,
            startTime = "02:30",
            endTime = "04:00",
            amOrPm = "PM",
            isStarred = true,
            format = "Workshop",
            level = "Advanced",
            startDate = DATE_2026_08_14,
            endDate = DATE_2026_08_14,
            remoteId = "remote_7",
            eventDay = "14",
            speakers =
                listOf(
                    SessionSpeakersPresentationModel(
                        name = "Kevin Omondi",
                        speakerImage = "",
                        twitterHandle = "kevinomondi",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "8",
            title = "Building Offline-First Apps",
            description = "Designing resilient mobile experiences.",
            venue = OPAL,
            startTime = "04:15",
            endTime = "05:00",
            amOrPm = "PM",
            isStarred = false,
            format = "Session",
            level = LEVEL_INTERMEDIATE,
            startDate = DATE_2026_08_14,
            endDate = DATE_2026_08_14,
            remoteId = "remote_8",
            eventDay = "14",
            speakers =
                listOf(
                    SessionSpeakersPresentationModel(
                        name = "Sarah Njeri",
                        speakerImage = "",
                        twitterHandle = "sarahcodes",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "9",
            title = "Day Two Keynote: AI on Android",
            description = "Exploring practical AI experiences on mobile.",
            venue = SAPPHIRE_OPAL,
            startTime = "09:00",
            endTime = "10:00",
            amOrPm = "AM",
            isStarred = true,
            format = "Keynote",
            level = "All Levels",
            startDate = DATE_2026_08_15,
            endDate = DATE_2026_08_15,
            remoteId = "remote_9",
            eventDay = "15",
            speakers =
                listOf(
                    SessionSpeakersPresentationModel(
                        name = "Grace Wambui",
                        speakerImage = "",
                        twitterHandle = "gracewambui",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "10",
            title = "Compose for Tablets and Foldables",
            description = "Adaptive UI patterns that scale.",
            venue = SAPPHIRE,
            startTime = "10:15",
            endTime = TIME_11_00,
            amOrPm = "AM",
            isStarred = false,
            format = "Talk",
            level = LEVEL_INTERMEDIATE,
            startDate = DATE_2026_08_15,
            endDate = DATE_2026_08_15,
            remoteId = "remote_10",
            eventDay = "15",
            speakers =
                listOf(
                    SessionSpeakersPresentationModel(
                        name = "Ian Otieno",
                        speakerImage = "",
                        twitterHandle = "ianotieno",
                    ),
                ),
        ),
    )