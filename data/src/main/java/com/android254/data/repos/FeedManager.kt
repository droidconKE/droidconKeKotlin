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

import com.android254.data.repos.mappers.toDomain
import com.android254.data.repos.mappers.toEntity
import com.android254.data.util.SyncException
import com.android254.data.util.sync
import com.android254.domain.models.Feed
import com.android254.domain.repos.FeedRepo
import com.android254.domain.sync.Synchronizer
import ke.droidcon.kotlin.datasource.local.source.LocalFeedDataSource
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.feed.RemoteFeedDataSource
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FeedManager
    @Inject
    constructor(
        private val localFeedDataSource: LocalFeedDataSource,
        private val remoteFeedDataSource: RemoteFeedDataSource,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : FeedRepo {
        override fun fetchFeed(): Flow<List<Feed>> =
            localFeedDataSource.fetchFeed().map { feeds -> feeds.map { it.toDomain() } }.flowOn(ioDispatcher)

        override fun fetchFeedById(id: Int): Flow<Feed?> =
            localFeedDataSource.getFeedById(id).map { feed -> feed?.toDomain() }.flowOn(ioDispatcher)

        override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
            synchronizer
                .sync(
                    remoteItemFetcher = {
                        when (val feedResponse = remoteFeedDataSource.fetchFeed()) {
                            is DataResult.Success -> feedResponse.data
                            is DataResult.Error -> throw SyncException(feedResponse.message, feedResponse.exc)
                            else -> throw SyncException("Sync feed failed")
                        }
                    },
                    localIdFetcher = { localFeedDataSource.getTitles() },
                    localItemUpserter = { remoteItems ->
                        localFeedDataSource.insertFeed(
                            feedItems = remoteItems.map { it.toEntity() },
                        )
                    },
                    localItemDeleter = { titles ->
                        localFeedDataSource.deleteByTitles(titles)
                    },
                    remoteToLocalIdSelector = { it.title },
                ).isSuccess
    }
