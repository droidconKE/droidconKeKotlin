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
package com.android254.shared.data.repos

import com.android254.shared.domain.models.HomeDetails
import com.android254.shared.domain.repos.HomeRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class HomeRepoImpl : HomeRepo {
    override fun fetchHomeDetails(): Flow<HomeDetails> {
        return flowOf(
            HomeDetails(
                isCallForSpeakersEnable = true,
                linkToCallForSpeakers = "https://droidcon.co.ke",
                isEventBannerEnable = true,
                speakers = emptyList(),
                speakersCount = 0,
                isSpeakersSessionEnable = false,
                sessions = emptyList(),
                sessionsCount = 0,
                isSessionsSectionEnable = false,
                sponsors = emptyList(),
                organizers = emptyList()
            )
        )
    }
}