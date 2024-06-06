package com.android254.shared.domain.models

data class HomeDetails(
    val isCallForSpeakersEnable: Boolean,
    val linkToCallForSpeakers: String,
    val isEventBannerEnable: Boolean,
    val sessions: List<Session>,
    val sessionsCount: Int,
    val isSessionsSectionEnable: Boolean,
    val speakers: List<Speaker>,
    val speakersCount: Int,
    val isSpeakersSessionEnable: Boolean,
    val sponsors: List<Sponsors>,
    val organizers: List<OrganizingPartners>
)
