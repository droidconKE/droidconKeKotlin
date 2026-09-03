/*
 * Copyright 2023 DroidconKE
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
    alias(libs.plugins.kotlin.serialization)
    alias(notation = libs.plugins.compose.compiler)
    alias(libs.plugins.droidconke.android.library.compose)
    alias(libs.plugins.droidconke.android.library.jacoco)
    alias(libs.plugins.compose.stability)
    alias(libs.plugins.droidconke.android.library.roborazzi)
}

android {
    namespace = "ke.droidcon.kotlin.presentation"

    defaultConfig {
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts.add("META-INF/io.netty.versions.properties")
        }
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.chai)
    implementation(projects.datasource.remote)

    implementation(libs.android.appCompat)
    implementation(libs.android.material)
    implementation(libs.lifecycle.runtimeKtx)
    implementation(libs.timber)
    implementation(libs.androidx.splashscreen)
    implementation(libs.kotlin.coroutines.play.services)
    implementation(libs.gms.play.services.auth)
    implementation(libs.lottie.compose)
    implementation(libs.kotlin.coroutines.datetime)
    implementation(libs.bundles.serialization)
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.coil)
    implementation(libs.compose.activity)
    implementation(libs.compose.constraintlayout)
    implementation(libs.compose.lifecycle.runtime)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.navigation)
    testImplementation(libs.test.mockk)
}

kotlin {
    compilerOptions {
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }
}