package com.android254.shared.domain.repos

import com.android254.shared.domain.models.FeedDomainModel

interface FeedRepository {
    fun fetchFeed(): Flow<List<FeedDomainModel>>
    fun fetchFeedById(id: Int): Flow<FeedDomainModel?>
    suspend fun syncFeed()
}