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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.android254.data.preferences.DefaultTokenProvider
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import ke.droidcon.kotlin.datasource.remote.feed.FeedApi
import ke.droidcon.kotlin.datasource.remote.organizers.OrganizersApi
import ke.droidcon.kotlin.datasource.remote.sessions.SessionsApi
import ke.droidcon.kotlin.datasource.remote.sponsors.SponsorsApi
import ke.droidcon.kotlin.datasource.remote.utils.HttpClientFactory
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import ke.droidcon.kotlin.datasource.remote.utils.TokenProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule: Module = module {
    single<ChuckerInterceptor> {
        ChuckerInterceptor.Builder(androidContext())
            .collector(ChuckerCollector(androidContext()))
            .maxContentLength(250000L)
            .redactHeaders(emptySet())
            .alwaysReadResponseBody(false)
            .build()
    }

    single<HttpClientEngine> {
        OkHttp.create { addInterceptor(get<ChuckerInterceptor>()) }
    }

    single<TokenProvider> {
        DefaultTokenProvider(get<DataStore<Preferences>>())
    }

    single<HttpClient> {
        HttpClientFactory(get<TokenProvider>(), get<RemoteFeatureToggle>()).create(get<HttpClientEngine>())
    }

    single<ke.droidcon.kotlin.datasource.remote.speakers.SpeakersApi> {
        ke.droidcon.kotlin.datasource.remote.speakers.SpeakersApi(get())
    }

    single<SessionsApi> { SessionsApi(get()) }


    single<SponsorsApi> { SponsorsApi(get()) }
    single<OrganizersApi> { OrganizersApi(get()) }
    single<FeedApi> { FeedApi(get()) }
}