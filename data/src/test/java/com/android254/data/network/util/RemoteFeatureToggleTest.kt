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
package com.android254.data.network.util

import com.android254.droidconKE2023.data.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class RemoteFeatureToggleTest {

    @Test
    fun `test successful api-key fetch`() {
        val remoteConfig: FirebaseRemoteConfig = mockk {
            every { getString(any()) } returns "default_api_key"
        }
        val remoteFeatureToggle = RemoteFeatureToggle(mockk(relaxed = true), remoteConfig)
        remoteFeatureToggle.getString("default_api_key")

        verify { remoteConfig.getString("default_api_key") }
    }

    @Test
    fun `test firebase remote config sync`() {
        val remoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
        val remoteFeatureToggle = RemoteFeatureToggle(mockk(relaxed = true), remoteConfig)
        remoteFeatureToggle.sync()

        verify { remoteConfig.setDefaultsAsync(R.xml.config) }
        verify { remoteConfig.setConfigSettingsAsync(any()) }
        verify { remoteConfig.fetchAndActivate() }
    }
}