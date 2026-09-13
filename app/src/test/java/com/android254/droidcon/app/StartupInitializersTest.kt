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
package com.android254.droidcon.app

import android.content.ComponentName
import android.content.pm.PackageManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StartupInitializersTest {
    @Test
    fun `startup provider keeps ProfileInstaller and drops only WorkManager's initializer`() {
        val context = RuntimeEnvironment.getApplication()
        val provider =
            context.packageManager.getProviderInfo(
                ComponentName(context, "androidx.startup.InitializationProvider"),
                PackageManager.GET_META_DATA,
            )
        val initializers = provider.metaData?.keySet().orEmpty()

        assertTrue(initializers.contains("androidx.profileinstaller.ProfileInstallerInitializer"))
        assertFalse(initializers.contains("androidx.work.WorkManagerInitializer"))
    }
}