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
package com.android254.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android254.data.dao.BookmarkDao
import com.android254.data.dao.OrganizersDao
import com.android254.data.dao.SessionDao
import com.android254.data.dao.SpeakerDao
import com.android254.data.db.model.BookmarkEntity
import com.android254.data.db.model.OrganizerEntity
import com.android254.data.db.model.SessionEntity
import com.android254.data.db.model.SpeakerEntity
import com.android254.data.db.util.InstantConverter

@Database(
    entities = [
        SessionEntity::class,
        SpeakerEntity::class,
        BookmarkEntity::class,
        OrganizerEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
@TypeConverters(
    InstantConverter::class
)
abstract class Database : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    abstract fun speakerDao(): SpeakerDao

    abstract fun bookmarkDao(): BookmarkDao

    abstract fun organizersDao(): OrganizersDao
}