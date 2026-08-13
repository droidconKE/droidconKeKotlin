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
    commonExtension.apply {
        compileSdk = libs.findVersion("android-compile-sdk").get().toString().toInt()

        defaultConfig.minSdk = libs.findVersion("android-min-sdk").get().toString().toInt()
        // Omitting this silently discovers no tests rather than failing.
        defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
        compileOptions.isCoreLibraryDesugaringEnabled = true

        configureManagedDevices(this)
        configureLint(this)
    }

    // Opt in with -PwarningsAsErrors=true.
    val warningsAsErrors = providers.gradleProperty("warningsAsErrors")
        .map(String::toBoolean)
        .orElse(false)

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors.set(warningsAsErrors)

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
        add("coreLibraryDesugaring", libs.findLibrary("desugar-jdk-libs").get())

        add("implementation", libs.findLibrary("android.coreKtx").get())

        // Every module, not just the Compose ones: lint.xml names these issue ids, and a
        // module without the checks fails with UnknownIssueId for each one.
        add("lintChecks", libs.findLibrary("compose-lint-checks").get())

        add("androidTestImplementation", libs.findLibrary("android.test.espresso").get())
        add("androidTestImplementation", libs.findLibrary("junit.androidx").get())
        add("androidTestImplementation", libs.findLibrary("junit.androidx.ktx").get())

        add("testImplementation", libs.findBundle("test").get())
        add("testImplementation", libs.findLibrary("android.test.espresso").get())
    }
}
