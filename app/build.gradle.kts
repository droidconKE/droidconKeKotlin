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
    alias(libs.plugins.droidconke.android.application)
    alias(libs.plugins.droidconke.android.application.compose)
    alias(libs.plugins.droidconke.android.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.droidconke.android.application.firebase)
    alias(libs.plugins.droidconke.android.application.jacoco)
    alias(libs.plugins.compose.stability)
    alias(libs.plugins.droidconke.android.library.roborazzi)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "ke.droidcon.kotlin"

    defaultConfig {
        applicationId = "ke.droidcon.kotlin"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    lint {
        checkDependencies = true
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
        }
        release {
            // Legacy DSL on purpose. AGP 9.3's optimization {} block is the documented
            // replacement, but the baseline profile plugin turns minification off for its
            // nonMinifiedRelease variant via isMinifyEnabled, and does not know about the new
            // block — so that variant gets minified anyway and generation produces a profile
            // full of repackaged names like La0;, which cannot match a shipped build.
            // Revisit when androidx.baselineprofile understands the new DSL.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts.add("META-INF/io.netty.versions.properties")
            pickFirsts.add("META-INF/INDEX.LIST")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

androidComponents {
    onVariants { variant ->
        if (variant.buildType != "debug") {
            variant.packaging.resources.excludes
                .addAll("**/*.proto", "**/*.kotlin_builtins")
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.domain)

    implementation(projects.feature.about)
    implementation(projects.feature.auth)
    implementation(projects.feature.feed)
    implementation(projects.feature.home)
    implementation(projects.feature.sessions)
    implementation(projects.feature.speakers)

    implementation(libs.androidx.profileinstaller)
    implementation(libs.android.coreKtx)
    implementation(libs.lifecycle.runtimeKtx)
    implementation(libs.timber)
    implementation(libs.work.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.kotlin.coroutines.play.services)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.identity.googleid)
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

    androidTestImplementation(libs.android.test.junit4)

    testImplementation(libs.bundles.test)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.navigation)
    testImplementation(libs.test.mockk)

    baselineProfile(projects.benchmarks)
}
kotlin {
    compilerOptions {
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }
}