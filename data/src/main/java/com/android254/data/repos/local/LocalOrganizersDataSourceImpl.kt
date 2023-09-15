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

import com.android254.data.dao.OrganizersDao
import com.android254.data.db.model.OrganizerEntity
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LocalOrganizersDataSourceImpl @Inject constructor(
    private val organizersDao: OrganizersDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalOrganizersDataSource {

    override fun getOrganizers(): Flow<List<OrganizerEntity>> {
        return organizersDao.fetchOrganizers()
            .flowOn(ioDispatcher)
    }

    override suspend fun deleteAllOrganizers() {
        withContext(ioDispatcher) {
            organizersDao.deleteAllOrganizers()
        }
    }

    override suspend fun insertOrganizers(organizers: List<OrganizerEntity>) {
        withContext(ioDispatcher) {
            organizersDao.insert(items = organizers)
        }
    }
}