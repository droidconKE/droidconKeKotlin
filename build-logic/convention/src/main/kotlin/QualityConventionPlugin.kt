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

import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * ktlint, detekt and spotless, configured identically everywhere.
 *
 * Applied by each project's own `plugins { }` block, including the root. This replaces the
 * `allprojects { apply(plugin = "...") }` blocks the root build file used to carry: imperative
 * application gets no type-safe accessors, and cross-project configuration is what stands
 * between this build and Isolated Projects.
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jlleitschuh.gradle.ktlint")
                apply("io.gitlab.arturbosch.detekt")
                apply("com.diffplug.spotless")
            }

            extensions.configure<KtlintExtension> {
                android.set(true)
                verbose.set(true)
                filter { exclude { element -> element.file.path.contains("generated/") } }
            }

            extensions.configure<DetektExtension> {
                config.setFrom(rootProject.file("detekt.yml"))
                parallel = true
                buildUponDefaultConfig = true
            }

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("**/*.kt")
                    targetExclude("**/build/**/*.kt")
                    licenseHeaderFile(
                        rootProject.file("spotless/copyright.kt"),
                        "^(package|object|import|interface)",
                    )
                }
                format("kts") {
                    target("**/*.kts")
                    targetExclude("**/build/**/*.kts")
                    // First line without a block comment is assumed to be where the licence ends.
                    licenseHeaderFile(
                        rootProject.file("spotless/copyright.kts"),
                        "(^(?![\\/ ]\\*).*$)",
                    )
                }
            }
        }
    }
}
