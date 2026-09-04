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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Adds Roborazzi's record/verify/compare tasks to a Compose library, plus the shared
 * harness in `:screenshot-testing`.
 *
 * `isIncludeAndroidResources` is already on for every library via
 * [AndroidLibraryConventionPlugin]; Roborazzi needs it and would fail without it.
 */
class AndroidLibraryRoborazziConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.github.takahirom.roborazzi")

            // Robolectric unpacks its native graphics runtime — the .so, ICU data and fonts —
            // under java.io.tmpdir. Gradle runs module test tasks in parallel, and sharing one
            // temp tree across those JVMs intermittently failed extraction with an IOException,
            // after which every capture in that JVM died on UnsatisfiedLinkError. A directory
            // per module keeps them out of each other's way.
            tasks.withType<Test>().configureEach {
                val tmp = layout.buildDirectory.dir("robolectric-tmp").get().asFile
                doFirst { tmp.mkdirs() }
                systemProperty("java.io.tmpdir", tmp.absolutePath)
            }

            dependencies {
                add("testImplementation", project(":screenshot-testing"))
            }
        }
    }
}
