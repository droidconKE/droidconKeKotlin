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

import com.android254.data.repos.*
import com.android254.domain.repos.*
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module

val repoModule = module {
    single { AuthManager(get(), get(), get<CoroutineDispatcher>(named("IoDispatcher"))) } binds arrayOf(AuthRepo::class)
    single { SessionsManager(get(), get(), get(), get<CoroutineDispatcher>(named("IoDispatcher"))) } binds arrayOf(SessionsRepo::class)
    single { HomeRepoImpl(get(), get(), get(), get(), get<CoroutineDispatcher>(named("IoDispatcher"))) } binds arrayOf(HomeRepo::class)
    single { SpeakersManager(get(), get()) } binds arrayOf(SpeakersRepo::class)
    single { OrganizersManager(get(), get(), get<CoroutineDispatcher>(named("IoDispatcher"))) } binds arrayOf(OrganizersRepo::class)
    single { FeedManager(get(), get()) } binds arrayOf(FeedRepo::class)
    single { SponsorsManager(get(), get()) } binds arrayOf(SponsorsRepo::class)
}