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
package ke.droidcon.kotlin.datasource.local.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ke.droidcon.kotlin.datasource.local.Database
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context,
    ): Database = buildDatabase(context, DATABASE_NAME)

    /**
     * The production configuration, shared with tests.
     *
     * Deliberately no `fallbackToDestructiveMigration()`: a missing migration must fail
     * loudly rather than silently delete the user's bookmarked sessions.
     */
    internal fun buildDatabase(
        context: Context,
        name: String,
    ): Database =
        Room
            .databaseBuilder(context, Database::class.java, name)
            .addMigrations(*Database.ALL_MIGRATIONS)
            .build()

    // Do not rename: it would orphan every installed user's data.
    internal const val DATABASE_NAME = "dcke22-database"
}