package com.android254.data.di

import com.android254.data.dao.SessionDao
import com.android254.data.dao.SpeakerDao
import com.android254.data.dao.SponsorsDao
import com.android254.data.network.apis.SessionsApi
import com.android254.data.network.apis.SpeakersApi
import com.android254.data.network.apis.SponsorsApi
import com.android254.data.repos.local.LocalSessionsDataSource
import com.android254.data.repos.local.LocalSessionsDataSourceImpl
import com.android254.data.repos.local.LocalSpeakersDataSource
import com.android254.data.repos.local.LocalSpeakersDataSourceImpl
import com.android254.data.repos.local.LocalSponsorsDataSource
import com.android254.data.repos.local.LocalSponsorsDataSourceImpl
import com.android254.data.repos.remote.RemoteSessionsDataSource
import com.android254.data.repos.remote.RemoteSessionsDataSourceImpl
import com.android254.data.repos.remote.RemoteSpeakersDataSource
import com.android254.data.repos.remote.RemoteSpeakersDataSourceImpl
import com.android254.data.repos.remote.RemoteSponsorsDataSource
import com.android254.data.repos.remote.RemoteSponsorsDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataSourcesModule {

    @Provides
    @Singleton
    fun provideRemoteSpeakersDataSource(
        api: SpeakersApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): RemoteSpeakersDataSource {
        return RemoteSpeakersDataSourceImpl(api = api, ioDispatcher = ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideRemoteSponsorsDataSource(
        api: SponsorsApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): RemoteSponsorsDataSource {
        return RemoteSponsorsDataSourceImpl(api = api, ioDispatcher = ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideRemoteSessionsDataSource(
        api: SessionsApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): RemoteSessionsDataSource {
        return RemoteSessionsDataSourceImpl(api = api, ioDispatcher = ioDispatcher)
    }


    @Provides
    @Singleton
    fun provideLocalSessionsDataSource(
        sessionDao: SessionDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): LocalSessionsDataSource {
        return LocalSessionsDataSourceImpl(sessionDao = sessionDao, ioDispatcher = ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideLocalSpeakersDataSource(
        speakersDao: SpeakerDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): LocalSpeakersDataSource {
        return LocalSpeakersDataSourceImpl(speakerDao = speakersDao, ioDispatcher = ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideLocalSponsorsDataSource(
        sponsorsDao: SponsorsDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): LocalSponsorsDataSource {
        return LocalSponsorsDataSourceImpl(sponsorsDao = sponsorsDao, ioDispatcher = ioDispatcher)
    }


}