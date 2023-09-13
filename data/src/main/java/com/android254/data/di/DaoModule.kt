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

import com.android254.data.dao.BookmarkDao
import com.android254.data.dao.FeedDao
import com.android254.data.dao.OrganizersDao
import com.android254.data.dao.SessionDao
import com.android254.data.dao.SpeakerDao
import com.android254.data.dao.SponsorsDao
import com.android254.data.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    fun provideSpeakersDao(
        database: Database
    ): SpeakerDao = database.speakerDao()

    @Provides
    fun provideSponsorsDao(
        database: Database
    ): SponsorsDao = database.sponsorsDao()

    @Provides
    fun providesAuthorDao(
        database: Database
    ): SessionDao = database.sessionDao()

    @Provides
    fun providesBookmarkDao(
        database: Database
    ): BookmarkDao = database.bookmarkDao()

    @Provides
    fun providesOrganizersDao(
        database: Database
    ): OrganizersDao = database.organizersDao()

    @Provides
    fun providesFeedDao(
        database: Database
    ): FeedDao = database.feedDao()
}