dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // ktlint-gradle publishes to the plugin portal only.
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include(":convention")
