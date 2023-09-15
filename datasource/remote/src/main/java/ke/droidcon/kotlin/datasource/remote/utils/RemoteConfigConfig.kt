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
package ke.droidcon.kotlin.datasource.remote.utils

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import java.util.concurrent.TimeUnit
import ke.droidcon.kotlin.datasource.remote.BuildConfig
import ke.droidcon.kotlin.datasource.remote.R

object RemoteConfigConfig {

    private val minimumFetchInterval = if (BuildConfig.DEBUG) {
        TimeUnit.MINUTES.toSeconds(5L)
    } else {
        TimeUnit.MINUTES.toSeconds(12L)
    }

    fun setup() = Firebase.remoteConfig.apply {
        setDefaultsAsync(R.xml.remote_config_defaults)
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = minimumFetchInterval
        }
        setConfigSettingsAsync(configSettings)
    }
}