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
import android.util.Log
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

const val TARGET_PACKAGE = "ke.droidcon.kotlin"
const val ITERATIONS = 15
const val WARMUP_ITERATIONS = 3
const val UI_TIMEOUT_MS = 10_000L
const val STARTUP_TIMEOUT_MS = 30_000L
private const val TAG = "DroidconBenchmarks"

fun MacrobenchmarkScope.openSessions() {
    device.requireObject("nav_sessions").click()
    check(device.wait(Until.hasObject(By.res("sessions_list")), UI_TIMEOUT_MS)) {
        "sessions_list never appeared after tapping nav_sessions"
    }
    device.waitForIdle()
}

/**
 * A missing node means the journey silently stopped exercising the app, which quietly produces
 * a profile covering nothing but launch. Fail instead.
 */
private fun UiDevice.requireObject(tag: String): UiObject2 {
    // The emulator composes the first frame well before the nav bar exists, so wait rather
    // than assume. A hard failure here beats a silent no-op producing a launch-only profile.
    wait(Until.hasObject(By.res(tag)), UI_TIMEOUT_MS)
    return findObject(By.res(tag))
        ?: error("No node with testTag '$tag'. Is testTagsAsResourceId set?")
}

/**
 * The journey the shipped profile is built from. Startup plus the first thing a user does on
 * each tab, so the profile covers more than the launch path.
 */
/**
 * Returns false if the app never got far enough to navigate, so the caller can report a
 * launch-only profile rather than silently shipping one.
 */
/**
 * MainActivity asks for POST_NOTIFICATIONS on every cold start, and macrobenchmark installs
 * fresh each iteration, so without this the permission dialog sits on top of the app and the
 * journey drives the permission controller instead of us. Granting is also closer to the
 * steady state we want a profile for. No-op below API 33, where the permission does not exist.
 */
fun MacrobenchmarkScope.grantNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    device.executeShellCommand("pm grant $TARGET_PACKAGE android.permission.POST_NOTIFICATIONS")
}

fun MacrobenchmarkScope.exerciseUserJourney(): Boolean {
    if (!device.wait(Until.hasObject(By.res("nav_home")), STARTUP_TIMEOUT_MS)) {
        Log.w(TAG, "Bottom navigation never appeared. Profile covers launch only.")
        return false
    }

    openSessions()
    device.findObject(By.res("sessions_list"))?.let { list ->
        list.setGestureMargin(device.displayWidth / 5)
        list.fling(Direction.DOWN)
        device.waitForIdle()
        list.fling(Direction.UP)
        device.waitForIdle()
    }

    device.requireObject("nav_feed").click()
    device.wait(Until.hasObject(By.res("feeds_lazy_column")), UI_TIMEOUT_MS)
    device.findObject(By.res("feeds_lazy_column"))?.fling(Direction.DOWN)
    device.waitForIdle()

    device.requireObject("nav_about").click()
    device.wait(Until.hasObject(By.res("about_screen")), UI_TIMEOUT_MS)
    device.waitForIdle()

    device.requireObject("nav_home").click()
    device.waitForIdle()
    return true
}
