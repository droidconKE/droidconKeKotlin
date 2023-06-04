package com.android254.data.repos.mappers

import com.android254.data.network.models.responses.FeedDTO
import com.android254.domain.models.Feed


fun FeedDTO.toDomain() = Feed(
    title = title,
    body = body,
    topic = topic,
    url = url,
    image = image,
    createdAt = createdAt.toString()
)