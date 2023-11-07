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

import com.android254.domain.models.Speaker
import com.android254.domain.models.Sponsors
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.models.SponsorPresentationModel

fun List<Speaker>.toSpeakersPresentation() =
    map {
        SpeakerUI(
            imageUrl = it.avatar,
            name = it.name,
            tagline = it.tagline,
            bio = it.biography,
            twitterHandle = it.twitter
        )
    }

fun Sponsors.toPresentation() = SponsorPresentationModel(
    name = sponsorName,
    link = link,
    logo = sponsorLogoUrl,
    sponsorType = sponsorType
)