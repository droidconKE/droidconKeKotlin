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