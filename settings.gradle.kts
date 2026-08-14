enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    // Fails the build if a subproject declares its own repositories, which is what the
    // `allprojects { repositories { } }` block used to do. Per-project repositories block
    // Isolated Projects and complicate the configuration cache.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "dcke"

include(":app")
include(":chai")
include(":datasource:remote", ":datasource:local", ":data", ":domain", ":presentation")