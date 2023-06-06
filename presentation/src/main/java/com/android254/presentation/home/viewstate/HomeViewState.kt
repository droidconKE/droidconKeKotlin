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
package com.android254.presentation.home.viewstate

import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SpeakerUI

data class HomeViewState(
    val isPosterVisible: Boolean = true,
    val isCallForSpeakersVisible: Boolean = false,
    val linkToCallForSpeakers: String = "",
    val isSignedIn: Boolean = false,
    val speakers: List<SpeakerUI> = emptyList(),
    val isSpeakersSectionVisible: Boolean = false,
    val sponsors: List<String> = emptyList(),
    val organizedBy: List<String> = emptyList(),
    val sessions: List<SessionPresentationModel> = emptyList(),
    val isSessionsSectionVisible: Boolean = false
)