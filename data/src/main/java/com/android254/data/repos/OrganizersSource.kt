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

import com.android254.data.dao.OrganizersDao
import com.android254.data.di.DefaultDispatcher
import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.OrganizersApi
import com.android254.data.repos.mappers.toDomain
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.DataResult
import com.android254.domain.repos.OrganizersRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OrganizersSource @Inject constructor(
    private val api: OrganizersApi,
    private val dao: OrganizersDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : OrganizersRepository {

    override suspend fun getOrganizers() = withContext(ioDispatcher) {
        val dbObjs = dao.fetchOrganizers()
        if (dbObjs.isEmpty()) {
            withContext(defaultDispatcher) {
                val result = api.fetchOrganizers("individual")
                if (result is DataResult.Success) {
                    val data = result.data
                    dao.insert(data.data.map { it.toEntity() })
                }
            }
            withContext(defaultDispatcher) {
                val result = api.fetchOrganizers("company")
                if (result is DataResult.Success) {
                    val data = result.data
                    dao.insert(data.data.map { it.toEntity() })
                }
            }
        }
        return@withContext dao.fetchOrganizers().map { it.toDomain() }
    }
}