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

class AndroidLibraryRoborazziConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.github.takahirom.roborazzi")

            // Robolectric unpacks its native runtime under java.io.tmpdir; parallel module
            // test JVMs sharing one temp tree intermittently fail that extraction.
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
