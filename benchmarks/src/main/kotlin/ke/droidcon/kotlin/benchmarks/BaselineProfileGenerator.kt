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
package ke.droidcon.kotlin.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    /**
     * Launch only. `includeInStartupProfile` marks everything a collection records as startup,
     * so this has to stay narrow — it drives dex layout, and pulling non-startup code into the
     * primary dex would defeat the point.
     */
    @Test
    fun startup() =
        rule.collect(
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = true,
        ) {
            grantNotificationPermission()
            pressHome()
            startActivityAndWait()
            device.waitForIdle()
        }

    /** The full journey, for AOT coverage beyond launch. */
    @Test
    fun baselineProfile() =
        rule.collect(packageName = TARGET_PACKAGE) {
            grantNotificationPermission()
            pressHome()
            startActivityAndWait()
            device.waitForIdle()

            exerciseUserJourney()
        }
}