/*
 * Copyright 2024 DroidconKE
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
package com.android254.shared.domain.models

data class HomeDetailsDomainModel(
    val isCallForSpeakersEnable: Boolean,
    val linkToCallForSpeakers: String,
    val isEventBannerEnable: Boolean,
    val sessions: List<SessionDomainModel>,
    val sessionsCount: Int,
    val isSessionsSectionEnable: Boolean,
    val speakers: List<SpeakerDomainModel>,
    val speakersCount: Int,
    val isSpeakersSessionEnable: Boolean,
    val sponsors: List<SponsorsDomainModel>,
    val organizers: List<OrganizingPartnersDomainModel>
)