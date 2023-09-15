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
package ke.droidcon.kotlin.datasource.remote.organizers

import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.organizers.model.OrganizersPagedResponse
import ke.droidcon.kotlin.datasource.remote.utils.DataResult

class RemoteOrganizersDataSourceImpl @Inject constructor(
    private val api: OrganizersApi
) : RemoteOrganizersDataSource {

    override suspend fun getIndividualOrganizers(): DataResult<OrganizersPagedResponse> =
        api.fetchOrganizers(type = "individual")

    override suspend fun getCompanyOrganizers(): DataResult<OrganizersPagedResponse> =
        api.fetchOrganizers(type = "company")
}