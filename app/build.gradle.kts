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
    alias(libs.plugins.droidconke.android.application)
    alias(libs.plugins.droidconke.android.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.droidconke.android.application.firebase)
    alias(libs.plugins.droidconke.android.application.jacoco)
}

android {
    namespace = "ke.droidcon.kotlin"

    defaultConfig {
        applicationId = "ke.droidcon.kotlin"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../keystore/dckedebug.keystore")
            keyAlias = "dcke"
            keyPassword = "droidconkenya"
            storePassword = "droidconkenya"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
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

    androidTestImplementation(libs.android.test.junit4)

    testImplementation(libs.bundles.test)
}