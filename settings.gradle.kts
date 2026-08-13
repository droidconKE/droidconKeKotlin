enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "dcke"

include(":app")
include(":chai")
include(":datasource:remote", ":datasource:local", ":data", ":domain", ":presentation")