package com.android254.presentation.models

data class FeedUI(
    val title: String,
    val body: String,
    val topic: String,
    val url: String,
    val image: String?,
    val createdAt: String
)
