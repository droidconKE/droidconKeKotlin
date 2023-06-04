package com.android254.presentation.feed.view

import com.android254.domain.models.Feed
import com.android254.presentation.models.FeedUI

fun Feed.toPresentation() = FeedUI(
    title = title,
    body = body,
    topic = topic,
    url = url,
    image = image,
    createdAt = createdAt
)