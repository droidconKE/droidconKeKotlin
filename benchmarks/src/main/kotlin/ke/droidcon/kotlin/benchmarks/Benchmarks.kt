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

import android.os.Build
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

const val TARGET_PACKAGE = "ke.droidcon.kotlin"
const val ITERATIONS = 15
const val WARMUP_ITERATIONS = 3
const val UI_TIMEOUT_MS = 10_000L
const val STARTUP_TIMEOUT_MS = 30_000L
const val FLING_MARGIN_DIVISOR = 5

/** `sessions_list` only exists once the first sync has data, so the wait gets the startup budget. */
fun MacrobenchmarkScope.openSessions() {
    device.requireObject("nav_sessions").click()
    device.requireObject("sessions_list", STARTUP_TIMEOUT_MS)
    device.waitForIdle()
}

/**
 * Both home sections have content, which is when `HomeScreen` reports fully drawn. Never throws:
 * offline there is nothing to wait for and `timeToFullDisplayMs` is simply not reported.
 */
fun MacrobenchmarkScope.waitForHomeContent() {
    device.wait(Until.hasObject(By.res("sessions")), STARTUP_TIMEOUT_MS)
    device.wait(Until.hasObject(By.res("speakersRow")), UI_TIMEOUT_MS)
}

/**
 * A missing node means the journey silently stopped exercising the app, which quietly produces
 * a profile covering nothing but launch. Fail instead.
 */
internal fun UiDevice.requireObject(
    tag: String,
    timeoutMs: Long = UI_TIMEOUT_MS,
): UiObject2 =
    wait(Until.findObject(By.res(tag)), timeoutMs)
        ?: error("No node with testTag '$tag' within ${timeoutMs}ms. Is testTagsAsResourceId set?")

/**
 * MainActivity asks for POST_NOTIFICATIONS on every cold start, and macrobenchmark installs
 * fresh each iteration, so without this the permission dialog sits on top of the app and the
 * journey drives the permission controller instead of us. Granting is also closer to the
 * steady state we want a profile for. No-op below API 33, where the permission does not exist.
 */
fun MacrobenchmarkScope.grantNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val output = device.executeShellCommand("pm grant $TARGET_PACKAGE android.permission.POST_NOTIFICATIONS")
    check(output.isBlank()) { "pm grant POST_NOTIFICATIONS failed: $output" }
}

/**
 * The journey the shipped profile is built from: startup plus the first thing a user does on
 * each tab, so the profile covers more than the launch path. Every step fails loudly, because a
 * step that quietly no-ops produces a launch-only profile that looks valid.
 */
fun MacrobenchmarkScope.exerciseUserJourney() {
    device.requireObject("nav_home", STARTUP_TIMEOUT_MS)

    openSessions()
    device.requireObject("sessions_list").apply {
        setGestureMargin(device.displayWidth / FLING_MARGIN_DIVISOR)
        fling(Direction.DOWN)
        device.waitForIdle()
        fling(Direction.UP)
        device.waitForIdle()
    }

    device.requireObject("nav_feed").click()
    device.requireObject("feeds_lazy_column").fling(Direction.DOWN)
    device.waitForIdle()

    device.requireObject("nav_about").click()
    device.requireObject("about_screen")
    device.waitForIdle()

    device.requireObject("nav_home").click()
    device.waitForIdle()
}