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
    alias(libs.plugins.droidconke.quality)
    alias(libs.plugins.droidconke.android.library)
    alias(libs.plugins.droidconke.android.hilt)
}

android {
    namespace = "ke.droidcon.kotlin.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlin.coroutines.datetime)
}

// Hilt gives this module a unit-test task, and Gradle 9 fails a test task that finds no tests.
// There is nothing here to test: a qualifier and two dispatcher providers.
tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}