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
    alias(libs.plugins.droidconke.android.feature)
}

android {
    namespace = "ke.droidcon.kotlin.feature.ask"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.a2ui.compose.ui)
    implementation(libs.a2ui.compose.runtime)
    implementation(libs.a2ui.material3)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(projects.core.testing)
    testImplementation(libs.test.mockk)
    testImplementation(libs.kotlin.coroutines.test)
}