/*
 * Copyright 2026 DroidconKE
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
import com.android254.domain.models.Ticket
import com.android254.domain.repos.AddTicketResult
import com.android254.domain.repos.TicketsRepo
import com.android254.domain.tickets.ticketNumberIn
import com.android254.domain.tickets.typedTicketNumber
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import ke.droidcon.kotlin.datasource.local.dao.TicketsDao
import ke.droidcon.kotlin.datasource.remote.tickets.TicketsApi
import ke.droidcon.kotlin.datasource.remote.tickets.model.IssuedTicketDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class TicketsManager
    @Inject
    constructor(
        private val ticketsApi: TicketsApi,
        private val ticketsDao: TicketsDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val clock: Clock,
    ) : TicketsRepo {
        private var refreshedAt: Instant? = null

        override fun ticket(): Flow<Ticket?> =
            ticketsDao
                .ticket()
                .map { it?.toDomain() }
                .flowOn(ioDispatcher)

        override suspend fun add(
            number: String,
            email: String,
        ): AddTicketResult {
            val ticketNumber = typedTicketNumber(number) ?: return AddTicketResult.NoMatch
            return lookUp(ticketNumber) { it.attendeeEmail.equals(email.trim(), ignoreCase = true) }
        }

        override suspend fun addScanned(code: String): AddTicketResult {
            val number = ticketNumberIn(code) ?: return AddTicketResult.NotATicket
            return lookUp(number) { true }
        }

        /** At most every few minutes: the lookup allows 30 a minute per IP, and a venue shares one. */
        override suspend fun refresh() {
            val now = clock.now()
            if (refreshedAt?.let { now - it < REFRESH_INTERVAL } == true) return
            withContext(ioDispatcher) {
                val saved = ticketsDao.current() ?: return@withContext
                val result = ticketsApi.findByNumber(saved.number) as? DataResult.Success ?: return@withContext
                result.data.issued(saved.number)?.let { ticketsDao.update(it.toEntity()) }
                refreshedAt = now
            }
        }

        override suspend fun remove() {
            withContext(ioDispatcher) { ticketsDao.clear() }
        }

        private suspend fun lookUp(
            number: String,
            belongs: (IssuedTicketDTO) -> Boolean,
        ): AddTicketResult =
            withContext(ioDispatcher) {
                when (val result = ticketsApi.findByNumber(number)) {
                    is DataResult.Success -> {
                        val issued = result.data.issued(number)?.takeIf(belongs) ?: return@withContext AddTicketResult.NoMatch
                        val entity = issued.toEntity()
                        ticketsDao.replace(entity)
                        AddTicketResult.Added(entity.toDomain())
                    }
                    is DataResult.Error ->
                        when {
                            result.rateLimited -> AddTicketResult.Busy
                            result.networkError -> AddTicketResult.Offline
                            else -> AddTicketResult.Failed
                        }
                    else -> AddTicketResult.Failed
                }
            }

        private fun List<IssuedTicketDTO>.issued(number: String) = firstOrNull { it.ticketNo.equals(number, ignoreCase = true) }
    }

private val REFRESH_INTERVAL = 5.minutes