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
package com.android254.presentation.common.bottomnav

import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionSpeakersPresentationModel

val fakeSessions = listOf(
    SessionPresentationModel(
        id = "1",
        title = "Modern Android Development",
        description = "A deep dive into MAD",
        venue = "Room 1",
        startTime = "09:00",
        endTime = "10:00",
        amOrPm = "AM",
        isStarred = false,
        format = "Keynote",
        level = "Beginner",
        startDate = "2023-11-16",
        endDate = "2023-11-16",
        remoteId = "remote_1",
        eventDay = "16",
        speakers = listOf(
            SessionSpeakersPresentationModel(
                name = "John Doe",
                speakerImage = "",
                twitterHandle = "johndoe"
            )
        )
    ),
    SessionPresentationModel(
        id = "2",
        title = "Compose Performance",
        description = "Optimizing your Compose UI",
        venue = "Room 2",
        startTime = "10:30",
        endTime = "11:30",
        amOrPm = "AM",
        isStarred = true,
        format = "Session",
        level = "Intermediate",
        startDate = "2023-11-16",
        endDate = "2023-11-16",
        remoteId = "remote_2",
        eventDay = "16",
        speakers = listOf(
            SessionSpeakersPresentationModel(
                name = "Jane Smith",
                speakerImage = "",
                twitterHandle = "janesmith"
            )
        )
    )
)
