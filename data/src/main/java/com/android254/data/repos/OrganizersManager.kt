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

import com.android254.data.repos.local.LocalOrganizersDataSource
import com.android254.data.repos.mappers.toDomain
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Organizer
import com.android254.domain.repos.OrganizersRepo
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.organizers.RemoteOrganizersDataSource
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class OrganizersManager @Inject constructor(
    private val localOrganizersDataSource: LocalOrganizersDataSource,
    private val remoteOrganizersDataSource: RemoteOrganizersDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : OrganizersRepo {

    override fun getOrganizers(): Flow<List<Organizer>> {
        return localOrganizersDataSource.getOrganizers()
            .map { it.map { it.toDomain() } }
    }

    override suspend fun syncOrganizers() {
        withContext(ioDispatcher) {
            val individualOrganizersResponseDeffered =
                async { remoteOrganizersDataSource.getIndividualOrganizers() }
            val companyOrganizersResponseDeffered =
                async { remoteOrganizersDataSource.getCompanyOrganizers() }

            val individualOrganizersResponse = individualOrganizersResponseDeffered.await()
            val companyOrganizersResponse = companyOrganizersResponseDeffered.await()
            if ((individualOrganizersResponse is DataResult.Success) && (companyOrganizersResponse is DataResult.Success)) {
                val individualOrganizers = individualOrganizersResponse.data.data
                val companyOrganizers = companyOrganizersResponse.data.data

                localOrganizersDataSource.insertOrganizers(organizers = individualOrganizers.map { it.toEntity() })
                localOrganizersDataSource.insertOrganizers(organizers = companyOrganizers.map { it.toEntity() })

                Timber.d(message = "Sync Organizers successful")
            }
        }
    }
}