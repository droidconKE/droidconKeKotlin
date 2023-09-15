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
package ke.droidcon.kotlin.datasource.remote.organizers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.organizers.model.OrganizersPagedResponse
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import ke.droidcon.kotlin.datasource.remote.utils.dataResultSafeApiCall
import ke.droidcon.kotlin.datasource.remote.utils.provideOrgBaseUrl

class OrganizersApi @Inject constructor(
    private val client: HttpClient
) {

    private val organizersUrl = "${provideOrgBaseUrl()}/team?"

    suspend fun fetchOrganizers(type: String): DataResult<OrganizersPagedResponse> =
        dataResultSafeApiCall {
            client.get("${organizersUrl}type=$type&per_page=100&page").body()
        }
}