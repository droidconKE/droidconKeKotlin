/*
 * Copyright 2026 DroidconKE
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
package com.android254.data.flags

import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RemoteFeatureFlagsTest {
    @Test
    fun `the cube switch reads its key once the defaults have applied`() =
        runTest {
            val toggle = mockk<RemoteFeatureToggle>()
            coEvery { toggle.awaitDefaults() } returns Unit
            every { toggle.getBoolean(any()) } returns false
            every { toggle.getBoolean(CUBE_HERO_KEY) } returns true

            assertTrue(RemoteFeatureFlags(toggle).isCubeHeroEnabled())
            coVerifyOrder {
                toggle.awaitDefaults()
                toggle.getBoolean(CUBE_HERO_KEY)
            }
        }

    @Test
    fun `the key has a default, so a fresh install shows the cube`() {
        val defaults = File("../network/src/main/res/xml/remote_config_defaults.xml").readText()

        assertTrue(defaults.contains("<key>$CUBE_HERO_KEY</key>"))
        assertFalse(defaults.substringAfter("<key>$CUBE_HERO_KEY</key>").substringBefore("</entry>").contains("false"))
    }

    private companion object {
        const val CUBE_HERO_KEY = "cube_hero_enabled"
    }
}