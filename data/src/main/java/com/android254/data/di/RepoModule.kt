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
package com.android254.data.di

import com.android254.data.repos.AuthManager
import com.android254.data.repos.HomeRepoImpl
import com.android254.data.repos.OrganizersSource
import com.android254.data.repos.SessionsManager
import com.android254.data.repos.SpeakersManager
import com.android254.domain.repos.AuthRepo
import com.android254.domain.repos.HomeRepo
import com.android254.domain.repos.OrganizersRepository
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun provideAuthRepo(repo: AuthManager): AuthRepo

    @Binds
    @Singleton
    abstract fun provideSessionsRepo(repo: SessionsManager): SessionsRepo

    @Binds
    @Singleton
    abstract fun providesHomeRepo(homeRepoImpl: HomeRepoImpl): HomeRepo

    @Binds
    @Singleton
    abstract fun provideSpeakersRepo(manager: SpeakersManager): SpeakersRepo

    @Binds
    @Singleton
    abstract fun provideOrganizersRepo(source: OrganizersSource): OrganizersRepository
}