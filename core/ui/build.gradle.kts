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
    alias(libs.plugins.droidconke.android.library.jacoco)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.stability)
}

android {
    namespace = "ke.droidcon.kotlin.core.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.designsystem)
    api(projects.domain)

    api(libs.bundles.navigation3)
    api(libs.kotlinx.collections.immutable)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.serialization)
    implementation(libs.compose.activity)
    implementation(libs.compose.lifecycle.runtime)
    implementation(libs.kotlin.coroutines.datetime)
    implementation(libs.lottie.compose)
    implementation(libs.timber)
}

kotlin {
    compilerOptions {
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }
}