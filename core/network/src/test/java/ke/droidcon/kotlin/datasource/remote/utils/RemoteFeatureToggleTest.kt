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
package ke.droidcon.kotlin.datasource.remote.utils

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteFeatureToggleTest {
    @Test
    fun `test successful api-key fetch`() {
        val remoteConfig: FirebaseRemoteConfig =
            mockk {
                every { getString(any()) } returns "default_api_key"
            }
        val remoteFeatureToggle = RemoteFeatureToggle(remoteConfig = remoteConfig)
        remoteFeatureToggle.getString("default_api_key")

        verify { remoteConfig.getString("default_api_key") }
    }

    @Test
    fun `the first launch waits for a fetch and asks for a sync`() =
        runTest {
            val remoteConfig = remoteConfigFetchedAt(-1L)

            assertTrue(RemoteFeatureToggle(remoteConfig).syncNowIfEmpty())
            verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        }

    @Test
    fun `later launches refresh in the background without a sync`() =
        runTest {
            val remoteConfig = remoteConfigFetchedAt(1_000L)

            assertFalse(RemoteFeatureToggle(remoteConfig).syncNowIfEmpty())
            verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        }

    private fun remoteConfigFetchedAt(millis: Long): FirebaseRemoteConfig {
        val info = mockk<FirebaseRemoteConfigInfo> { every { fetchTimeMillis } returns millis }
        val initialised = completedTask(info)
        val fetched = completedTask(true)
        return mockk {
            every { ensureInitialized() } returns initialised
            every { fetchAndActivate() } returns fetched
            every { this@mockk.info } returns info
        }
    }

    private fun <T> completedTask(value: T): Task<T> =
        mockk(relaxed = true) {
            every { isComplete } returns true
            every { isCanceled } returns false
            every { exception } returns null
            every { result } returns value
        }
}