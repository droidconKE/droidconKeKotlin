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