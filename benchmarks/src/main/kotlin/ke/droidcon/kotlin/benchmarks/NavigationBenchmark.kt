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

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Jank while moving between tabs, and while the cube plays on About. */
@RunWith(AndroidJUnit4::class)
class NavigationBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun switchTabs() =
        rule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.Ignore(),
            setupBlock = {
                grantNotificationPermission()
                pressHome()
                startActivityAndWait()
                waitForHomeContent()
            },
        ) {
            listOf(ABOUT, "nav_feed", "nav_sessions", ABOUT, "nav_home").forEach { tab ->
                device.requireObject(tab).click()
                device.waitForIdle()
            }
        }

    @Test
    fun cubeOnAbout() =
        rule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.Ignore(),
            setupBlock = {
                grantNotificationPermission()
                pressHome()
                startActivityAndWait()
            },
        ) {
            device.requireObject(ABOUT, STARTUP_TIMEOUT_MS).click()
            device.requireObject("about_screen")
            // One scene: a hold, the twist to the next face and the solve.
            Thread.sleep(CUBE_SCENE_MS)
        }

    private companion object {
        const val CUBE_SCENE_MS = 6_500L
        const val ABOUT = "nav_about"
    }
}