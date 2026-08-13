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
 * ```
 * ./gradlew :app:supportedApiLevelsGroupDebugAndroidTest
 * ```
 */
internal fun configureManagedDevices(commonExtension: CommonExtension) {
    // AGP 9 drops the configuration-block form on CommonExtension; these are properties now.
    val managedDevices = commonExtension.testOptions.managedDevices
    val localDevices = managedDevices.localDevices

    localDevices.create("api30") {
        device = "Pixel 4"
        apiLevel = 30
        systemImageSource = "aosp-atd"
        testedAbi = HOST_ABI
    }
    localDevices.create("api34") {
        device = "Pixel 6"
        apiLevel = 34
        systemImageSource = "aosp-atd"
        testedAbi = HOST_ABI
    }

    managedDevices.groups.create("supportedApiLevels") {
        targetDevices.addAll(localDevices)
    }
}

/**
 * AGP 9 warns that an unset `testedAbi` changes default in AGP 10. Keyed to the host rather
 * than pinned to x86_64 so CI runners and Apple Silicon each resolve a native image.
 */
private val HOST_ABI: String =
    if (System.getProperty("os.arch") in setOf("aarch64", "arm64")) "arm64-v8a" else "x86_64"
