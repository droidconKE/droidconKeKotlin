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

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import ke.droidcon.kotlin.datasource.remote.R
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class RemoteFeatureToggle(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    private val defaults: Task<Void> = remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

    /**
     * Done once `remote_config_defaults.xml` applies. The defaults load on a background thread, and
     * until they have, every switch reads as off and the API key as empty.
     */
    suspend fun awaitDefaults() {
        runCatching { defaults.await() }.onFailure { Timber.w(it, "Remote Config defaults failed to apply") }
    }

    fun sync() {
        remoteConfig
            .fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.w("Successfully fetched remote config from Firebase")
                } else {
                    Timber.w("Failed to fetch remote config from Firebase")
                }
            }
    }

    /**
     * Waits for the first fetch, since the API key has no usable default, and returns true so the
     * caller syncs with it. Once fetched, refreshes in the background so switches reach installs.
     */
    suspend fun syncNowIfEmpty(): Boolean {
        awaitDefaults()
        remoteConfig.ensureInitialized().await()
        if (remoteConfig.info.fetchTimeMillis <= 0L) {
            remoteConfig.fetchAndActivate().await()
            return true
        }
        sync()
        return false
    }

    fun getString(key: String): String = remoteConfig.getString(key)

    fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)
}