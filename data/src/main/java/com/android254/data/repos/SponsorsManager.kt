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
import com.android254.domain.models.Sponsors
import com.android254.domain.repos.SponsorsRepo
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.local.source.LocalSponsorsDataSource
import ke.droidcon.kotlin.datasource.remote.sponsors.RemoteSponsorsDataSource
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SponsorsManager @Inject constructor(
    private val localSponsorsDataSource: LocalSponsorsDataSource,
    private val remoteSponsorsDataSource: RemoteSponsorsDataSource
) : SponsorsRepo {

    override fun getAllSponsors(): Flow<List<Sponsors>> =
        localSponsorsDataSource.fetchCachedSponsors().map { sponsors -> sponsors.map { it.toDomain() } }

    override suspend fun syncSponsors() {
        when (val response = remoteSponsorsDataSource.getAllSponsorsRemote()) {
            is DataResult.Success -> {
                localSponsorsDataSource.deleteCachedSponsors()
                localSponsorsDataSource.saveCachedSponsors(
                    sponsors = response.data.map { sponsors -> sponsors.toEntity() }
                )
                Timber.d("Sync sponsors successful")
            }

            is DataResult.Error -> {
                Timber.d("Sync sponsors failed ${response.message}")
            }

            else -> {
            }
        }
    }
}