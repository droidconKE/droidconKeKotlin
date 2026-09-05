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

import com.android254.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Everything a :feature: module gets by default. A feature declares only what is genuinely
// its own; if two features need the same thing, that thing belongs in :core:ui.
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("droidconke.quality")
                apply("droidconke.android.library")
                apply("droidconke.android.library.compose")
                apply("droidconke.android.library.jacoco")
                apply("droidconke.android.hilt")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("com.github.skydoves.compose.stability.analyzer")
            }

            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions.optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
            }

            dependencies {
                add("implementation", project(":core:ui"))

                add("implementation", libs.findLibrary("compose.lifecycle.runtime").get())
                add("implementation", libs.findLibrary("timber").get())
                add("implementation", libs.findBundle("coil").get())
            }
        }
    }
}
