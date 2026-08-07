/*
 * Copyright 2022 DroidconKE
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
import com.android254.domain.models.Organizer
import com.android254.domain.repos.OrganizersRepo
import com.android254.domain.sync.Synchronizer
import ke.droidcon.kotlin.datasource.local.source.LocalOrganizersDataSource
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.organizers.RemoteOrganizersDataSource
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OrganizersManager
    @Inject
    constructor(
        private val localOrganizersDataSource: LocalOrganizersDataSource,
        private val remoteOrganizersDataSource: RemoteOrganizersDataSource,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : OrganizersRepo {
        override fun getOrganizers(): Flow<List<Organizer>> =
            localOrganizersDataSource
                .getOrganizers()
                .map { it.distinctBy { organizer -> organizer.name }.map { organizer -> organizer.toDomain() } }
                .flowOn(ioDispatcher)

        override suspend fun syncWith(synchronizer: Synchronizer): Boolean =
            synchronizer
                .sync(
                    remoteItemFetcher = {
                        val individualOrganizersResponse = remoteOrganizersDataSource.getIndividualOrganizers()
                        val companyOrganizersResponse = remoteOrganizersDataSource.getCompanyOrganizers()

                        if (individualOrganizersResponse is DataResult.Success && companyOrganizersResponse is DataResult.Success) {
                            individualOrganizersResponse.data.data + companyOrganizersResponse.data.data
                        } else {
                            val errorMessage =
                                when {
                                    individualOrganizersResponse is DataResult.Error -> individualOrganizersResponse.message
                                    companyOrganizersResponse is DataResult.Error -> companyOrganizersResponse.message
                                    else -> "Sync Organizers failed"
                                }
                            val cause =
                                (individualOrganizersResponse as? DataResult.Error)?.exc
                                    ?: (companyOrganizersResponse as? DataResult.Error)?.exc
                            throw SyncException(errorMessage, cause)
                        }
                    },
                    localIdFetcher = { localOrganizersDataSource.getNames() },
                    localItemUpserter = { remoteItems ->
                        localOrganizersDataSource.insertOrganizers(
                            organizers = remoteItems.map { it.toEntity() },
                        )
                    },
                    localItemDeleter = { names ->
                        localOrganizersDataSource.deleteByNames(names)
                    },
                    remoteToLocalIdSelector = { it.name ?: "" },
                ).isSuccess
    }
