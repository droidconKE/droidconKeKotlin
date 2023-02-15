plugins {
    `kotlin-dsl`
}

group = "com.android254.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "droidconke.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}