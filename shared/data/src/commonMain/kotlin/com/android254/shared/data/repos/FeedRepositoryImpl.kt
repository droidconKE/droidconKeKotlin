/*
 * Copyright 2024 DroidconKE
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