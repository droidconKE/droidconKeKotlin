/*
 * Copyright 2022 The Android Open Source Project
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
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    // AGP 9's CommonExtension exposes these as properties but no longer as configuration
    // blocks — `defaultConfig { }` and friends live only on the concrete Application and
    // Library extensions. Property access is what works against the shared supertype.
    commonExtension.apply {
        compileSdk = libs.findVersion("android-compile-sdk").get().toString().toInt()

        defaultConfig.minSdk = libs.findVersion("android-min-sdk").get().toString().toInt()
        // Without this AGP falls back to the JUnit3 runner, which silently discovers
        // no @RunWith(AndroidJUnit4::class) tests — a green run that tested nothing.
        defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
        // java.time is native at minSdk 26, so this now only backports the newer additions.
        compileOptions.isCoreLibraryDesugaringEnabled = true

        configureManagedDevices(this)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            val warningsAsErrors: String? by project
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())

            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
            )

            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    dependencies {
        // Backports the newer java.time additions; the core API is native at minSdk 26.
        add("coreLibraryDesugaring", libs.findLibrary("desugar-jdk-libs").get())

        add("implementation", libs.findLibrary("android.coreKtx").get())

        add("androidTestImplementation", libs.findLibrary("android.test.espresso").get())
        add("androidTestImplementation", libs.findLibrary("junit.androidx").get())
        add("androidTestImplementation", libs.findLibrary("junit.androidx.ktx").get())

        add("testImplementation", libs.findBundle("test").get())
        add("testImplementation", libs.findLibrary("android.test.espresso").get())
    }
}
