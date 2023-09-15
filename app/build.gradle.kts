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
    id("droidconke.android.application")
    id("droidconke.android.hilt")
    id("droidconke.android.application.firebase")
}

android {
    namespace = "ke.droidcon.kotlin"
    compileSdk = 34

    defaultConfig {
        applicationId = "ke.droidcon.kotlin"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packagingOptions {
        resources {
            pickFirsts.add("META-INF/io.netty.versions.properties")
            pickFirsts.add("META-INF/INDEX.LIST")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":chai"))
    implementation(project(":data"))
    implementation(project(":datasource:local"))
    implementation(project(":datasource:remote"))
    implementation(project(":domain"))
    implementation(project(":presentation"))

    implementation(libs.android.coreKtx)
    implementation(libs.android.appCompat)
    implementation(libs.android.material)

    implementation(libs.timber)

    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    implementation(libs.hilt.common)

    androidTestImplementation(libs.android.test.junit4)
    androidTestImplementation(libs.android.test.espresso)

    testImplementation(libs.bundles.test)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}