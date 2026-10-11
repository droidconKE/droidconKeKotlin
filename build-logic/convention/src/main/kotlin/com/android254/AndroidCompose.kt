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
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    commonExtension.buildFeatures.compose = true

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))

        add("implementation", libs.findBundle("compose").get())
        add("debugImplementation", libs.findLibrary("compose.ui.test.manifest").get())
        add("testImplementation", libs.findLibrary("compose.ui.test.junit").get())
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
            optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            metricsDestination.set(buildDirWhenEnabled("enableComposeCompilerMetrics", "compose-metrics"))
            reportsDestination.set(buildDirWhenEnabled("enableComposeCompilerReports", "compose-reports"))
        }
    }
}

private fun Project.buildDirWhenEnabled(
    property: String,
    dir: String,
): Provider<Directory> =
    providers
        .gradleProperty(property)
        .filter(String::toBoolean)
        .flatMap { layout.buildDirectory.dir(dir) }
