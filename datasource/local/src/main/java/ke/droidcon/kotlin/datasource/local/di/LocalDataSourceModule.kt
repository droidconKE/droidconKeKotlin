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

package ke.droidcon.kotlin.datasource.local.di

import ke.droidcon.kotlin.datasource.local.dao.FeedDao
import ke.droidcon.kotlin.datasource.local.dao.OrganizersDao
import ke.droidcon.kotlin.datasource.local.dao.SessionDao
import ke.droidcon.kotlin.datasource.local.dao.SpeakerDao
import ke.droidcon.kotlin.datasource.local.dao.SponsorsDao
import ke.droidcon.kotlin.datasource.local.source.LocalFeedDataSource
import ke.droidcon.kotlin.datasource.local.source.LocalFeedDataSourceImpl
import ke.droidcon.kotlin.datasource.local.source.LocalOrganizersDataSource
import ke.droidcon.kotlin.datasource.local.source.LocalOrganizersDataSourceImpl
import ke.droidcon.kotlin.datasource.local.source.LocalSessionsDataSource
import ke.droidcon.kotlin.datasource.local.source.LocalSessionsDataSourceImpl
import ke.droidcon.kotlin.datasource.local.source.LocalSpeakersDataSource
import ke.droidcon.kotlin.datasource.local.source.LocalSpeakersDataSourceImpl
import ke.droidcon.kotlin.datasource.local.source.LocalSponsorsDataSource
import ke.droidcon.kotlin.datasource.local.source.LocalSponsorsDataSourceImpl
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module

val localDataSourceModule: Module = module {
    single { LocalFeedDataSourceImpl(get<FeedDao>(), get<CoroutineDispatcher>(named("LocalSourceIoDispatcher"))) } binds arrayOf(LocalFeedDataSource::class)
    single { LocalOrganizersDataSourceImpl(get<OrganizersDao>(), get<CoroutineDispatcher>(named("LocalSourceIoDispatcher"))) } binds arrayOf(LocalOrganizersDataSource::class)
    single { LocalSessionsDataSourceImpl(get<SessionDao>(), get<CoroutineDispatcher>(named("LocalSourceIoDispatcher"))) } binds arrayOf(LocalSessionsDataSource::class)
    single { LocalSpeakersDataSourceImpl(get<SpeakerDao>(), get<CoroutineDispatcher>(named("LocalSourceIoDispatcher"))) } binds arrayOf(LocalSpeakersDataSource::class)
    single { LocalSponsorsDataSourceImpl(get<SponsorsDao>(), get<CoroutineDispatcher>(named("LocalSourceIoDispatcher"))) } binds arrayOf(LocalSponsorsDataSource::class)
}