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
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

private const val SAPPHIRE_OPAL = "Sapphire,Opal"
private const val SAPPHIRE = "Sapphire"
private const val OPAL = "Opal"

val today = LocalDate.now()
val yesterday = today.minusDays(1)
val tomorrow = today.plusDays(1)

val DATE_YESTERDAY = yesterday.toString()
val DATE_TODAY = today.toString()
val DATE_TOMORROW = tomorrow.toString()

val DAY_YESTERDAY = yesterday.dayOfMonth.toString()
val DAY_TODAY = today.dayOfMonth.toString()
val DAY_TOMORROW = tomorrow.dayOfMonth.toString()

private const val TIME_08_00 = "08:00"
private const val TIME_09_00 = "09:00"
private const val TIME_09_30 = "09:30"
private const val TIME_10_00 = "10:00"
private const val TIME_10_15 = "10:15"
private const val TIME_11_00 = "11:00"
private const val TIME_11_30 = "11:30"
private const val TIME_12_15 = "12:15"
private const val TIME_01_30 = "01:30"
private const val TIME_02_15 = "02:15"
private const val TIME_02_30 = "02:30"
private const val TIME_04_00 = "04:00"
private const val TIME_04_15 = "04:15"
private const val TIME_05_00 = "05:00"

private const val LEVEL_INTERMEDIATE = "Intermediate"
private const val LEVEL_ALL = "All Levels"
private const val LEVEL_ADVANCED = "Advanced"
private const val LEVEL_BEGINNER = "Beginner"

private const val FORMAT_KEYNOTE = "Keynote"
private const val FORMAT_TALK = "Talk"
private const val FORMAT_BREAK = "Break"
private const val FORMAT_SESSION = "Session"

private const val AM = "AM"
private const val PM = "PM"

val fakeSessions =
    persistentListOf(
        // Day 1: Yesterday
        SessionPresentationModel(
            id = "1",
            title = "Opening Keynote: Building for Africa",
            description = "The future of Android development across Africa.",
            venue = SAPPHIRE_OPAL,
            startTime = TIME_09_00,
            endTime = TIME_10_00,
            amOrPm = AM,
            isStarred = true,
            format = FORMAT_KEYNOTE,
            level = LEVEL_ALL,
            sessionStatus = SessionStatus.Past,
            startDate = DATE_YESTERDAY,
            endDate = DATE_YESTERDAY,
            remoteId = "remote_1",
            eventDay = DAY_YESTERDAY,
            speakers =
                persistentListOf(
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
            startTime = TIME_10_15,
            endTime = TIME_11_00,
            amOrPm = AM,
            isStarred = false,
            sessionStatus = SessionStatus.Past,
            format = FORMAT_TALK,
            level = LEVEL_INTERMEDIATE,
            startDate = DATE_YESTERDAY,
            endDate = DATE_YESTERDAY,
            remoteId = "remote_2",
            eventDay = DAY_YESTERDAY,
            speakers =
                persistentListOf(
                    SessionSpeakersPresentationModel(
                        name = "Donald Okara",
                        speakerImage = "",
                        twitterHandle = "donnycodes",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "3",
            title = "Modern Android Architecture",
            description = "Best practices for building scalable apps.",
            venue = OPAL,
            startTime = TIME_01_30,
            endTime = TIME_02_15,
            amOrPm = PM,
            isStarred = false,
            sessionStatus = SessionStatus.Past,
            format = FORMAT_TALK,
            level = LEVEL_ADVANCED,
            startDate = DATE_YESTERDAY,
            endDate = DATE_YESTERDAY,
            remoteId = "remote_3",
            eventDay = DAY_YESTERDAY,
            speakers =
                persistentListOf(
                    SessionSpeakersPresentationModel(
                        name = "Sarah Njeri",
                        speakerImage = "",
                        twitterHandle = "sarahcodes",
                    ),
                ),
        ),
        // Day 2: Today
        SessionPresentationModel(
            id = "4",
            title = "Coffee Break",
            description = "Grab coffee and network with attendees.",
            venue = OPAL,
            startTime = TIME_08_00,
            endTime = TIME_09_00,
            amOrPm = AM,
            isStarred = false,
            sessionStatus = SessionStatus.Past,
            format = FORMAT_BREAK,
            level = "",
            startDate = DATE_TODAY,
            endDate = DATE_TODAY,
            remoteId = "remote_4",
            isService = true,
            eventDay = DAY_TODAY,
            speakers = persistentListOf(),
        ),
        SessionPresentationModel(
            id = "5",
            title = "Kotlin Multiplatform in Production",
            description = "Lessons from shipping KMP apps at scale.",
            venue = SAPPHIRE,
            startTime = TIME_09_30,
            endTime = TIME_11_30,
            amOrPm = AM,
            isStarred = true,
            sessionStatus = SessionStatus.Ongoing,
            format = FORMAT_SESSION,
            level = LEVEL_ADVANCED,
            startDate = DATE_TODAY,
            endDate = DATE_TODAY,
            remoteId = "remote_5",
            eventDay = DAY_TODAY,
            speakers =
                persistentListOf(
                    SessionSpeakersPresentationModel(
                        name = "Brian Kimani",
                        speakerImage = "",
                        twitterHandle = "briankimani",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "6",
            title = "Lunch Break",
            description = "Lunch served at the networking hall.",
            venue = SAPPHIRE_OPAL,
            startTime = TIME_12_15,
            endTime = TIME_01_30,
            amOrPm = PM,
            isStarred = false,
            format = FORMAT_BREAK,
            level = "",
            sessionStatus = SessionStatus.Upcoming,
            startDate = DATE_TODAY,
            endDate = DATE_TODAY,
            remoteId = "remote_6",
            isService = true,
            eventDay = DAY_TODAY,
            speakers = persistentListOf(),
        ),
        SessionPresentationModel(
            id = "7",
            title = "State Management in Compose",
            description = "Handling complex UI state without pain.",
            venue = OPAL,
            startTime = TIME_02_30,
            endTime = TIME_04_00,
            amOrPm = PM,
            sessionStatus = SessionStatus.Upcoming,
            isStarred = false,
            format = FORMAT_TALK,
            level = LEVEL_BEGINNER,
            startDate = DATE_TODAY,
            endDate = DATE_TODAY,
            remoteId = "remote_7",
            eventDay = DAY_TODAY,
            speakers =
                persistentListOf(
                    SessionSpeakersPresentationModel(
                        name = "Amina Yusuf",
                        speakerImage = "",
                        twitterHandle = "aminadev",
                    ),
                ),
        ),
        // Day 3: Tomorrow
        SessionPresentationModel(
            id = "8",
            title = "Day Two Keynote: AI on Android",
            description = "Exploring practical AI experiences on mobile.",
            venue = SAPPHIRE_OPAL,
            startTime = TIME_09_00,
            endTime = TIME_10_00,
            amOrPm = AM,
            isStarred = true,
            format = FORMAT_KEYNOTE,
            level = LEVEL_ALL,
            sessionStatus = SessionStatus.Upcoming,
            startDate = DATE_TOMORROW,
            endDate = DATE_TOMORROW,
            remoteId = "remote_8",
            eventDay = DAY_TOMORROW,
            speakers =
                persistentListOf(
                    SessionSpeakersPresentationModel(
                        name = "Grace Wambui",
                        speakerImage = "",
                        twitterHandle = "gracewambui",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "9",
            title = "Compose for Tablets and Foldables",
            description = "Adaptive UI patterns that scale.",
            venue = SAPPHIRE,
            startTime = TIME_10_15,
            endTime = TIME_11_00,
            amOrPm = AM,
            isStarred = false,
            sessionStatus = SessionStatus.Upcoming,
            format = FORMAT_TALK,
            level = LEVEL_INTERMEDIATE,
            startDate = DATE_TOMORROW,
            endDate = DATE_TOMORROW,
            remoteId = "remote_9",
            eventDay = DAY_TOMORROW,
            speakers =
                persistentListOf(
                    SessionSpeakersPresentationModel(
                        name = "Ian Otieno",
                        speakerImage = "",
                        twitterHandle = "ianotieno",
                    ),
                ),
        ),
        SessionPresentationModel(
            id = "10",
            title = "Closing Remarks \u0026 Giveaways",
            description = "Wrapping up Droidcon Kenya with prizes!",
            venue = SAPPHIRE_OPAL,
            startTime = TIME_04_15,
            endTime = TIME_05_00,
            amOrPm = PM,
            isStarred = false,
            format = FORMAT_SESSION,
            level = LEVEL_ALL,
            sessionStatus = SessionStatus.Upcoming,
            startDate = DATE_TOMORROW,
            endDate = DATE_TOMORROW,
            remoteId = "remote_10",
            eventDay = DAY_TOMORROW,
            speakers = persistentListOf(),
        ),
    )