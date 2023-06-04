package com.android254.data.repos

import com.android254.data.network.apis.FeedApi
import com.android254.data.repos.mappers.toDomain
import com.android254.domain.models.DataResult
import com.android254.domain.models.Feed
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.FeedRepo
import javax.inject.Inject

class FeedManager @Inject constructor(
    private val api: FeedApi
) : FeedRepo {
    override suspend fun fetchFeed(): ResourceResult<List<Feed>> {
        return when (val result = api.fetchFeed(1, 100)) {
            is DataResult.Empty -> {
                ResourceResult.Success(emptyList())
            }
            is DataResult.Error -> {
                ResourceResult.Error(
                    result.message,
                    networkError = result.message.contains("network", ignoreCase = true)
                )
            }
            is DataResult.Success -> {
                val data = result.data
                if (data.isNotEmpty()) {
                    ResourceResult.Empty()
                }
                ResourceResult.Success(
                    data.map { it.toDomain() }
                )
            }

            else -> {
                ResourceResult.Success(emptyList())
            }
        }
    }
}