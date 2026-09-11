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
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollSessionsList() =
        rule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            startupMode = StartupMode.WARM,
            // Ignore, not Partial: forcing a compilation mode needs `cmd package compile`,
            // which some OEM builds refuse. Jank is measured in whatever state the app is in.
            compilationMode = CompilationMode.Ignore(),
            setupBlock = {
                grantNotificationPermission()
                pressHome()
                startActivityAndWait()
                openSessions()
            },
        ) {
            val list = device.requireObject("sessions_list")
            list.setGestureMargin(device.displayWidth / FLING_MARGIN_DIVISOR)
            repeat(SCROLL_COUNT) {
                list.fling(Direction.DOWN)
                device.waitForIdle()
            }
            list.fling(Direction.UP)
            device.waitForIdle()
        }

    private companion object {
        const val SCROLL_COUNT = 4
    }
}