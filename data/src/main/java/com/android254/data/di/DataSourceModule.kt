/*
 * Copyright 2023 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android254.data.di

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
import ke.droidcon.kotlin.datasource.remote.feed.RemoteFeedDataSource
import ke.droidcon.kotlin.datasource.remote.feed.RemoteFeedDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.organizers.RemoteOrganizersDataSource
import ke.droidcon.kotlin.datasource.remote.organizers.RemoteOrganizersDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.sessions.RemoteSessionsDataSource
import ke.droidcon.kotlin.datasource.remote.sessions.RemoteSessionsDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.speakers.RemoteSpeakersDataSource
import ke.droidcon.kotlin.datasource.remote.speakers.RemoteSpeakersDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.sponsors.RemoteSponsorsDataSource
import ke.droidcon.kotlin.datasource.remote.sponsors.RemoteSponsorsDataSourceImpl
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataSourceModule: Module = module {
    single<RemoteSpeakersDataSource> {
        RemoteSpeakersDataSourceImpl(
            api = get(),
            ioDispatcher = get(named("IoDispatcher"))
        )
    }
    single<RemoteSponsorsDataSource> {
        RemoteSponsorsDataSourceImpl(
            api = get(),
            ioDispatcher = get(named("IoDispatcher"))
        )
    }
    single<RemoteSessionsDataSource> {
        RemoteSessionsDataSourceImpl(
            api = get(),
            ioDispatcher = get(named("IoDispatcher"))
        )
    }
    single<LocalSessionsDataSource> {
        LocalSessionsDataSourceImpl(
            sessionDao = get(),
            localSourceIoDispatcher = get(named("IoDispatcher"))
        )
    }
    single<LocalSpeakersDataSource> {
        LocalSpeakersDataSourceImpl(
            speakerDao = get(),
            localSourceIoDispatcher = get(named("IoDispatcher"))
        )
    }
    single<LocalSponsorsDataSource> {
        LocalSponsorsDataSourceImpl(
            sponsorsDao = get(),
            localSourceIoDispatcher = get(named("IoDispatcher"))
        )
    }
    single<LocalOrganizersDataSource> {
        LocalOrganizersDataSourceImpl(
            organizersDao = get(),
            localSourceIoDispatcher = get(named("IoDispatcher"))
        )
    }
    single<RemoteOrganizersDataSource> { RemoteOrganizersDataSourceImpl(api = get()) }
    single<RemoteFeedDataSource> { RemoteFeedDataSourceImpl(api = get()) }
    single<LocalFeedDataSource> {
        LocalFeedDataSourceImpl(
            feedDao = get(),
            ioDispatcher = get(named("IoDispatcher"))
        )
    }
}
