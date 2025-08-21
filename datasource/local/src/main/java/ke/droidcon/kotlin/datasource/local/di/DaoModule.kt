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

import ke.droidcon.kotlin.datasource.local.Database
import ke.droidcon.kotlin.datasource.local.dao.BookmarkDao
import ke.droidcon.kotlin.datasource.local.dao.FeedDao
import ke.droidcon.kotlin.datasource.local.dao.OrganizersDao
import ke.droidcon.kotlin.datasource.local.dao.SessionDao
import ke.droidcon.kotlin.datasource.local.dao.SpeakerDao
import ke.droidcon.kotlin.datasource.local.dao.SponsorsDao
import org.koin.dsl.module
import org.koin.core.module.Module

val daoModule: Module = module {
    single<SpeakerDao> { get<Database>().speakerDao() }
    single<SponsorsDao> { get<Database>().sponsorsDao() }
    single<SessionDao> { get<Database>().sessionDao() }
    single<BookmarkDao> { get<Database>().bookmarkDao() }
    single<OrganizersDao> { get<Database>().organizersDao() }
    single<FeedDao> { get<Database>().feedDao() }
}