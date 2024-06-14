package com.android254.shared.domain.models

data class FeedDomainModel(
    val title: String,
    val body: String,
    val topic: String,
    val url: String,
    val image: String?,
    val createdAt: String
)
