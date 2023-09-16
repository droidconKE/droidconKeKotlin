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

import com.android254.domain.models.Sponsors
import ke.droidcon.kotlin.datasource.remote.sponsors.model.SponsorDTO
import kotlinx.coroutines.flow.Flow

interface LocalSponsorsDataSource {

    fun fetchCachedSponsors(): Flow<List<Sponsors>>

    suspend fun deleteCachedSponsors()

    suspend fun saveCachedSponsors(sponsors: List<SponsorDTO>)
}