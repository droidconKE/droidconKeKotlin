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
import com.android254.data.network.util.HttpClientFactory
import com.android254.data.network.util.TokenProvider
import com.android254.data.preferences.DefaultTokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClientEngine(): HttpClientEngine = Android.create {
        connectTimeout = 10_000
    }

    @Provides
    @Singleton
    fun provideHttpClient(engine: HttpClientEngine, tokenProvider: TokenProvider): HttpClient = HttpClientFactory(tokenProvider).create(engine)

    @Provides
    @Singleton
    fun provideTokenProvider(datastore: DataStore<Preferences>): TokenProvider = DefaultTokenProvider(datastore)
}