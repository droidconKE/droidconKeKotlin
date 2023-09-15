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

import com.android254.data.dao.SponsorsDao
import com.android254.data.repos.mappers.toDomain
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Sponsors
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.sponsors.model.SponsorDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalSponsorsDataSourceImpl @Inject constructor(
    private val sponsorsDao: SponsorsDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalSponsorsDataSource {

    override fun fetchCachedSponsors(): Flow<List<Sponsors>> {
        return sponsorsDao.fetchCachedSponsors()
            .flowOn(ioDispatcher)
            .map {
                it.map { it.toDomain() }
            }
    }

    override suspend fun deleteCachedSponsors() {
        withContext(ioDispatcher) {
            sponsorsDao.deleteAllCachedSponsors()
        }
    }

    override suspend fun saveCachedSponsors(sponsors: List<SponsorDTO>) {
        withContext(ioDispatcher) {
            sponsorsDao.insert(items = sponsors.map { it.toEntity() })
        }
    }
}