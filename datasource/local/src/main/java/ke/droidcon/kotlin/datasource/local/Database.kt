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
package ke.droidcon.kotlin.datasource.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ke.droidcon.kotlin.datasource.local.dao.BookmarkDao
import ke.droidcon.kotlin.datasource.local.dao.FeedDao
import ke.droidcon.kotlin.datasource.local.dao.OrganizersDao
import ke.droidcon.kotlin.datasource.local.dao.SessionDao
import ke.droidcon.kotlin.datasource.local.dao.SpeakerDao
import ke.droidcon.kotlin.datasource.local.dao.SponsorsDao
import ke.droidcon.kotlin.datasource.local.model.BookmarkEntity
import ke.droidcon.kotlin.datasource.local.model.FeedEntity
import ke.droidcon.kotlin.datasource.local.model.OrganizerEntity
import ke.droidcon.kotlin.datasource.local.model.SessionEntity
import ke.droidcon.kotlin.datasource.local.model.SpeakerEntity
import ke.droidcon.kotlin.datasource.local.model.SponsorEntity
import ke.droidcon.kotlin.datasource.local.util.InstantConverter

@Database(
    entities = [
        SessionEntity::class,
        SpeakerEntity::class,
        BookmarkEntity::class,
        OrganizerEntity::class,
        SponsorEntity::class,
        FeedEntity::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
@TypeConverters(
    InstantConverter::class
)
abstract class Database : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    abstract fun speakerDao(): SpeakerDao

    abstract fun bookmarkDao(): BookmarkDao

    abstract fun organizersDao(): OrganizersDao

    abstract fun sponsorsDao(): SponsorsDao

    abstract fun feedDao(): FeedDao
}