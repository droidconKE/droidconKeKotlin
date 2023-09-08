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
package com.android254.data.repos.local

import com.android254.data.dao.FeedDao
import com.android254.data.network.models.responses.FeedDTO
import com.android254.data.repos.mappers.toDomain
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Feed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalFeedDataSourceImpl @Inject constructor(
    private val feedDao: FeedDao,
    private val ioDispatcher: CoroutineDispatcher
) : LocalFeedDataSource {

    override suspend fun deleteAllFeed() {
        withContext(ioDispatcher) {
            feedDao.deleteAllFeed()
        }
    }

    override suspend fun insertFeed(feedItems: List<FeedDTO>) {
        withContext(ioDispatcher) {
            feedDao.insert(items = feedItems.map { it.toEntity() })
        }
    }

    override fun fetchFeed(): Flow<List<Feed>> =
        feedDao.fetchFeed().map { it.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun getFeedById(feedId: Int): Flow<Feed?> =
        feedDao.fetchFeedById(feedId).map { it?.toDomain() }
            .flowOn(ioDispatcher)
}