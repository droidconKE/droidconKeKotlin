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

import com.google.android.gms.tasks.OnCompleteListener
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
            val fetch = completedTask(true)
            val remoteConfig = remoteConfigFetchedAt(-1L, fetch)

            assertTrue(RemoteFeatureToggle(remoteConfig).syncNowIfEmpty())
            verify(exactly = 1) { remoteConfig.fetchAndActivate() }
            verify(exactly = 0) { fetch.addOnCompleteListener(any<OnCompleteListener<Boolean>>()) }
        }

    @Test
    fun `later launches refresh in the background without a sync`() =
        runTest {
            val fetch = completedTask(true)
            val remoteConfig = remoteConfigFetchedAt(1_000L, fetch)

            assertFalse(RemoteFeatureToggle(remoteConfig).syncNowIfEmpty())
            verify(exactly = 1) { fetch.addOnCompleteListener(any<OnCompleteListener<Boolean>>()) }
        }

    @Test
    fun `a failed first fetch is reported and tried again next time`() =
        runTest {
            val failed: Task<Boolean> =
                mockk(relaxed = true) {
                    every { isComplete } returns true
                    every { isCanceled } returns false
                    every { exception } returns IllegalStateException("offline")
                }
            val remoteConfig = remoteConfigFetchedAt(-1L, failed)
            val toggle = RemoteFeatureToggle(remoteConfig)

            repeat(2) { assertTrue(runCatching { toggle.syncNowIfEmpty() }.isFailure) }
            verify(exactly = 2) { remoteConfig.fetchAndActivate() }
        }

    private fun remoteConfigFetchedAt(
        millis: Long,
        fetch: Task<Boolean>,
    ): FirebaseRemoteConfig {
        val info = mockk<FirebaseRemoteConfigInfo> { every { fetchTimeMillis } returns millis }
        val initialised = completedTask(info)
        return mockk {
            every { ensureInitialized() } returns initialised
            every { fetchAndActivate() } returns fetch
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