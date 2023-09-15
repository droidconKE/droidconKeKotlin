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
package com.android254.data.di

import com.android254.data.dao.FeedDao
import com.android254.data.dao.OrganizersDao
import com.android254.data.dao.SessionDao
import com.android254.data.dao.SpeakerDao
import com.android254.data.dao.SponsorsDao
import com.android254.data.repos.local.LocalFeedDataSource
import com.android254.data.repos.local.LocalFeedDataSourceImpl
import com.android254.data.repos.local.LocalOrganizersDataSource
import com.android254.data.repos.local.LocalOrganizersDataSourceImpl
import com.android254.data.repos.local.LocalSessionsDataSource
import com.android254.data.repos.local.LocalSessionsDataSourceImpl
import com.android254.data.repos.local.LocalSpeakersDataSource
import com.android254.data.repos.local.LocalSpeakersDataSourceImpl
import com.android254.data.repos.local.LocalSponsorsDataSource
import com.android254.data.repos.local.LocalSponsorsDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.feed.FeedApi
import ke.droidcon.kotlin.datasource.remote.feed.RemoteFeedDataSource
import ke.droidcon.kotlin.datasource.remote.feed.RemoteFeedDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.organizers.OrganizersApi
import ke.droidcon.kotlin.datasource.remote.organizers.RemoteOrganizersDataSource
import ke.droidcon.kotlin.datasource.remote.organizers.RemoteOrganizersDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.sessions.RemoteSessionsDataSource
import ke.droidcon.kotlin.datasource.remote.sessions.RemoteSessionsDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.sessions.SessionsApi
import ke.droidcon.kotlin.datasource.remote.speakers.RemoteSpeakersDataSource
import ke.droidcon.kotlin.datasource.remote.speakers.RemoteSpeakersDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.speakers.SpeakersApi
import ke.droidcon.kotlin.datasource.remote.sponsors.RemoteSponsorsDataSource
import ke.droidcon.kotlin.datasource.remote.sponsors.RemoteSponsorsDataSourceImpl
import ke.droidcon.kotlin.datasource.remote.sponsors.SponsorsApi
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideRemoteSpeakersDataSource(
        api: SpeakersApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): RemoteSpeakersDataSource =
        RemoteSpeakersDataSourceImpl(api = api, ioDispatcher = ioDispatcher)

    @Provides
    @Singleton
    fun provideRemoteSponsorsDataSource(
        api: SponsorsApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): RemoteSponsorsDataSource =
        RemoteSponsorsDataSourceImpl(api = api, ioDispatcher = ioDispatcher)

    @Provides
    @Singleton
    fun provideRemoteSessionsDataSource(
        api: SessionsApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): RemoteSessionsDataSource =
        RemoteSessionsDataSourceImpl(api = api, ioDispatcher = ioDispatcher)

    @Provides
    @Singleton
    fun provideLocalSessionsDataSource(
        sessionDao: SessionDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): LocalSessionsDataSource =
        LocalSessionsDataSourceImpl(sessionDao = sessionDao, ioDispatcher = ioDispatcher)

    @Provides
    @Singleton
    fun provideLocalSpeakersDataSource(
        speakersDao: SpeakerDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): LocalSpeakersDataSource =
        LocalSpeakersDataSourceImpl(speakerDao = speakersDao, ioDispatcher = ioDispatcher)

    @Provides
    @Singleton
    fun provideLocalSponsorsDataSource(
        sponsorsDao: SponsorsDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): LocalSponsorsDataSource =
        LocalSponsorsDataSourceImpl(sponsorsDao = sponsorsDao, ioDispatcher = ioDispatcher)

    @Provides
    @Singleton
    fun provideLocalOrganizersDataSource(
        organizersDao: OrganizersDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): LocalOrganizersDataSource =
        LocalOrganizersDataSourceImpl(organizersDao = organizersDao, ioDispatcher = ioDispatcher)

    @Provides
    @Singleton
    fun provideRemoteOrganizersDataSource(
        api: OrganizersApi
    ): RemoteOrganizersDataSource = RemoteOrganizersDataSourceImpl(api = api)

    @Provides
    @Singleton
    fun provideFeedDataSource(
        api: FeedApi
    ): RemoteFeedDataSource = RemoteFeedDataSourceImpl(api = api)

    @Provides
    @Singleton
    fun provideLocalFeedDataSource(
        feedDao: FeedDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): LocalFeedDataSource = LocalFeedDataSourceImpl(feedDao = feedDao, ioDispatcher = ioDispatcher)
}