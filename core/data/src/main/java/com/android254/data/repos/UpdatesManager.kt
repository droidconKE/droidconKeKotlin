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

import com.android254.domain.models.ScheduleUpdate
import com.android254.domain.repos.UpdatesRepo
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import ke.droidcon.kotlin.datasource.local.dao.UpdatesDao
import ke.droidcon.kotlin.datasource.local.model.UpdateEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Instant

class UpdatesManager
    @Inject
    constructor(
        private val updatesDao: UpdatesDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : UpdatesRepo {
        override fun scheduleUpdates(): Flow<List<ScheduleUpdate>> =
            updatesDao
                .updates()
                .map { rows -> rows.mapNotNull { it.toDomain() } }
                .flowOn(ioDispatcher)
    }

/** A kind this version does not know, written by a newer one, is skipped rather than guessed at. */
internal fun UpdateEntity.toDomain(): ScheduleUpdate? =
    ScheduleUpdate.Kind.entries.firstOrNull { it.name == kind }?.let { known ->
        ScheduleUpdate(id, known, sessionId, title, count, Instant.fromEpochMilliseconds(createdAt))
    }