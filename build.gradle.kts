// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // On the classpath for the droidconke.quality convention plugin, which is what applies and
    // configures all three in every module. ktlint is applied here too, for this file and
    // settings.gradle.kts — the root cannot apply a convention plugin without putting the whole
    // of build-logic on its classpath, which breaks every module's versioned plugin alias.
    alias(libs.plugins.jlleitschuh)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.hilt.plugin) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.compose.stability) apply false
    // `./gradlew dependencyUpdates`
    alias(libs.plugins.toml.checker)
    alias(libs.plugins.toml.updater)
}

ktlint {
    android.set(true)
    verbose.set(true)
    filter {
        exclude { element -> element.file.path.contains("generated/") }
    }
}