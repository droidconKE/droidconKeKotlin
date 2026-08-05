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
import com.android254.data.util.sync
import com.android254.domain.models.Sponsors
import com.android254.domain.repos.SponsorsRepo
import com.android254.domain.sync.Synchronizer
import ke.droidcon.kotlin.datasource.local.source.LocalSponsorsDataSource
import ke.droidcon.kotlin.datasource.remote.sponsors.RemoteSponsorsDataSource
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SponsorsManager
    @Inject
    constructor(
        private val localSponsorsDataSource: LocalSponsorsDataSource,
        private val remoteSponsorsDataSource: RemoteSponsorsDataSource,
    ) : SponsorsRepo {
        override fun getAllSponsors(): Flow<List<Sponsors>> =
            localSponsorsDataSource.fetchCachedSponsors().map { sponsors -> sponsors.map { it.toDomain() } }

        override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
            synchronizer.sync(
                remoteItemFetcher = {
                    val response = remoteSponsorsDataSource.getAllSponsorsRemote()
                    if (response is DataResult.Success) {
                        response.data
                    } else {
                        throw Exception("Sync sponsors failed")
                    }
                },
                localIdFetcher = { localSponsorsDataSource.getNames() },
                localItemUpserter = { remoteItems ->
                    localSponsorsDataSource.saveCachedSponsors(
                        sponsors = remoteItems.map { it.toEntity() },
                    )
                },
                localItemDeleter = { names ->
                    localSponsorsDataSource.deleteByNames(names)
                },
                remoteToLocalIdSelector = { it.name },
            ).isSuccess
    }