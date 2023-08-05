package com.android254.data.di

import com.android254.data.work.SyncDataWorkManagerImpl
import com.android254.domain.work.SyncDataWorkManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class WorkModule {

    @Binds
    abstract fun provideSyncDataWorkManager(impl:SyncDataWorkManagerImpl): SyncDataWorkManager
}