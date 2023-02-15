plugins {
    id("droidconke.android.library")
    id("com.google.devtools.ksp")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.android254.droidconKE2023.data"

    defaultConfig {

        if (File("api_key.txt").exists()) {
            buildConfigField("String", "API_KEY", "\"${File("api_key.txt").readText().trim()}\"")
        } else {
            buildConfigField("String", "API_KEY", "\"\"")
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.android.appCompat)
    implementation(libs.android.material)
    api(libs.kotlin.coroutines.datetime)
    implementation(libs.android.hilt)
    implementation(libs.timber)
    kapt(libs.android.hilt.compiler)
    implementation(libs.datastore)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.ktor.core)
    implementation(libs.ktor.android)
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.json)
    implementation(libs.ktor.auth)
    implementation(libs.ktor.logging)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.test.androidx.core)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.ktor.mock)
    testImplementation(libs.test.mockk)
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }
}