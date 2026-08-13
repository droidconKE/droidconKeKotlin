/*
 * Copyright 2026 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android254

import com.android.build.api.dsl.CommonExtension

/**
 * Emulators for instrumentation tests, provisioned by Gradle rather than by CI.
 *
 * `minSdk` is included deliberately — a crash there is only possible if nothing tests it.
 *
 * ```
 * ./gradlew :app:supportedApiLevelsGroupDebugAndroidTest
 * ./gradlew :app:api24DebugAndroidTest
 * ```
 */
internal fun configureManagedDevices(commonExtension: CommonExtension) {
    // AGP 9 drops the configuration-block form on CommonExtension; these are properties now.
    val managedDevices = commonExtension.testOptions.managedDevices
    val localDevices = managedDevices.localDevices

    // ATD images are unavailable below API 30, so the floor uses AOSP.
    localDevices.create("api24") {
        device = "Pixel 2"
        apiLevel = MIN_SDK
        systemImageSource = "aosp"
        // AGP otherwise picks the 32-bit x86 image, which emulators no longer run.
        require64Bit = true
    }
    localDevices.create("api30") {
        device = "Pixel 4"
        apiLevel = 30
        systemImageSource = "aosp-atd"
    }
    localDevices.create("api34") {
        device = "Pixel 6"
        apiLevel = 34
        systemImageSource = "aosp-atd"
    }

    managedDevices.groups.create("supportedApiLevels") {
        targetDevices.addAll(localDevices)
    }
}

private const val MIN_SDK = 24
