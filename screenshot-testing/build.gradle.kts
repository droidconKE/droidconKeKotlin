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
    alias(libs.plugins.droidconke.android.library.compose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "ke.droidcon.kotlin.screenshot"
}

// Consumed only through `testImplementation`, so none of this reaches the app.
dependencies {
    api(libs.bundles.roborazzi)
    api(libs.test.robolectric)
    api(libs.compose.ui.test.junit)
    api(libs.test.androidx.core)
    api(libs.junit4)
    api(libs.coil.test)
    api(libs.coil.compose)

    implementation(projects.chai)
}