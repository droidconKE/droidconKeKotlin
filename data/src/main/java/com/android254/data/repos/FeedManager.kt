/*
 * Copyright 2023 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android254.data.repos

import com.android254.data.repos.local.LocalFeedDataSource
import com.android254.data.repos.remote.RemoteFeedDataSource
import com.android254.domain.models.DataResult
import com.android254.domain.models.Feed
import com.android254.domain.repos.FeedRepo
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class FeedManager @Inject constructor(
    private val localFeedDataSource: LocalFeedDataSource,
    private val remoteFeedDataSource: RemoteFeedDataSource
) : FeedRepo {

    override fun fetchFeed(): Flow<List<Feed>> =
        localFeedDataSource.fetchFeed()

    override fun fetchFeedById(id: Int): Flow<Feed?> =
        localFeedDataSource.getFeedById(id)

    override suspend fun syncFeed() {
        val feedResponse = remoteFeedDataSource.fetchFeed()
        when (feedResponse) {
            is DataResult.Success -> {
                localFeedDataSource.deleteAllFeed()
                localFeedDataSource.insertFeed(feedItems = feedResponse.data)
                Timber.d("Sync feed successful")
            }
            is DataResult.Empty -> {
            }
            is DataResult.Loading -> {
            }
            is DataResult.Error -> {
                Timber.d("Sync feed error ${feedResponse.message}")
            }
        }
    }
}