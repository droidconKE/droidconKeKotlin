plugins {
    id("droidconke.android.library")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.android254.droidconKE2023.domain"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}