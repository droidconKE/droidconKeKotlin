enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "DroidconKE2023"

include(":app")
include(":chai")
include(":datasource:remote", ":datasource:local", ":data", ":domain", ":presentation")
include(":shared", ":shared:data", ":shared:domain", ":shared:datasource")