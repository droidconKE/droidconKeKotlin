package com.android254.shared.data.repos

import com.android254.shared.domain.models.FeedDomainModel
import com.android254.shared.domain.repos.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FeedRepositoryImpl : FeedRepository {
    override fun fetchFeed(): Flow<List<FeedDomainModel>> {
        return flowOf(
            listOf(
                FeedDomainModel(
                    title = "After Party",
                    body = "We will have an after party, All are welcomed.",
                    topic = "",
                    url = "",
                    image = "",
                    createdAt = ""
                )
            )
        )
    }

    override fun fetchFeedById(id: Int): Flow<FeedDomainModel?> {
        return flowOf(
            FeedDomainModel(
                title = "After Party",
                body = "We will have an after party, All are welcomed.",
                topic = "",
                url = "",
                image = "",
                createdAt = ""
            )
        )
    }

    override suspend fun syncFeed() {
        val feeds = mutableListOf<FeedDomainModel>()
        feeds.add(
            FeedDomainModel(
                title = "After Party",
                body = "We will have an after party, All are welcomed.",
                topic = "",
                url = "",
                image = "",
                createdAt = ""
            )
        )
    }
}