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
package com.android254.data.network.models.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class EventScheduleGroupedResponse(
    val data: Map<String, List<SessionDTO>>
)

@Serializable
data class SessionDTO(
    val id: String,
    val backgroundColor: String,
    val borderColor: String,
    val description: String,
    @SerialName("end_date_time") val endDateTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("is_bookmarked") val isBookmarked: Boolean,
    @SerialName("is_keynote") val isKeynote: Boolean,
    @SerialName("is_serviceSession") val isServiceSession: Boolean,
    @SerialName("session_format") val sessionFormat: String,
    @SerialName("session_image") val sessionImage: String?,
    @SerialName("session_level") val sessionLevel: String,
    val slug: String,
    @SerialName("start_date_time") val startDateTime: String,
    @SerialName("start_time") val startTime: String,
    val title: String,
    val rooms: List<RoomDTO>,
    val speakers: List<SpeakerDTO>
)