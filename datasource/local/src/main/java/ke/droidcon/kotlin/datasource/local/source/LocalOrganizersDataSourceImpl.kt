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
package ke.droidcon.kotlin.datasource.local.source

import javax.inject.Inject
import ke.droidcon.kotlin.datasource.local.dao.OrganizersDao
import ke.droidcon.kotlin.datasource.local.di.LocalSourceIoDispatcher
import ke.droidcon.kotlin.datasource.local.model.OrganizerEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LocalOrganizersDataSourceImpl @Inject constructor(
    private val organizersDao: OrganizersDao,
    @LocalSourceIoDispatcher private val localSourceIoDispatcher: CoroutineDispatcher
) : LocalOrganizersDataSource {

    override fun getOrganizers(): Flow<List<OrganizerEntity>> {
        return organizersDao.fetchOrganizers()
            .flowOn(localSourceIoDispatcher)
    }

    override suspend fun deleteAllOrganizers() {
        withContext(localSourceIoDispatcher) {
            organizersDao.deleteAllOrganizers()
        }
    }

    override suspend fun insertOrganizers(organizers: List<OrganizerEntity>) {
        withContext(localSourceIoDispatcher) {
            organizersDao.insert(items = organizers)
        }
    }
}