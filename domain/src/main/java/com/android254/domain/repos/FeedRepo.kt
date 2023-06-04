package com.android254.domain.repos

import com.android254.domain.models.Feed
import com.android254.domain.models.ResourceResult

interface FeedRepo {
    suspend fun fetchFeed(): ResourceResult<List<Feed>>
}