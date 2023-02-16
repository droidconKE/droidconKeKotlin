plugins {
    `kotlin-dsl`
}

group = "com.android254.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
//sourceCompatibility = <MAJOR_JDK_VERSION>
//targetCompatibility = <MAJOR_JDK_VERSION>

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