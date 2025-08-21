
/*
 * Copyright 2023 DroidconKE
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
package com.android254.droidconKE2023.app

import android.content.Context
import com.android254.data.di.dataSourceModule
import com.android254.data.di.datastoreModule
import com.android254.data.di.networkModule
import com.android254.data.di.remoteConfigModule
import com.android254.data.di.repoModule
import com.android254.data.di.workModule
import com.android254.presentation.di.authModule
import com.android254.presentation.di.presentationModule
import ke.droidcon.kotlin.datasource.local.di.daoModule
import ke.droidcon.kotlin.datasource.local.di.databaseModule
import ke.droidcon.kotlin.datasource.local.di.dispatcherModule
import ke.droidcon.kotlin.datasource.local.di.localDataSourceModule
import ke.droidcon.kotlin.datasource.remote.di.remoteDispatcherModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


fun initKoin(context: Context) {
    startKoin {
        androidLogger()
        androidContext(context)
        modules(
            dispatcherModule,
            remoteDispatcherModule,
            dataSourceModule,
            datastoreModule,
            networkModule,
            remoteConfigModule,
            repoModule,
            workModule,
            daoModule,
            databaseModule,
            localDataSourceModule,
            presentationModule,
            authModule
        )
    }
}
