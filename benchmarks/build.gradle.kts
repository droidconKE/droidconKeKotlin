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
plugins {
    id("com.android.test")
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "ke.droidcon.kotlin.benchmarks"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    // Generation needs API 33+ (or root). A full aosp image, not aosp-atd: profile collection
    // needs a rooted shell, which the stripped test image does not give us.
    testOptions.managedDevices.localDevices.create("benchmarkApi34") {
        device = "Pixel 6"
        apiLevel = 34
        systemImageSource = "aosp"
        testedAbi = if (System.getProperty("os.arch") in setOf("aarch64", "arm64")) "arm64-v8a" else "x86_64"
    }
}

baselineProfile {
    // Generate on the emulator so the profile is reproducible and CI can rebuild it. Timings
    // from an emulator are not trustworthy, so StartupBenchmark is run on real hardware instead.
    managedDevices += "benchmarkApi34"
    useConnectedDevices = false
}

kotlin {
    compilerOptions {
        optIn.add("androidx.benchmark.macro.ExperimentalMacrobenchmarkApi")
        optIn.add("androidx.benchmark.macro.ExperimentalMetricApi")
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.uiautomator)
    implementation(libs.junit4)
    implementation(libs.android.test.junit4)
}
