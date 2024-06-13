plugins {
    `kotlin-dsl`
}

group = "com.android254.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.firebase.crashlytics.gradlePlugin)
    compileOnly(libs.firebase.performance.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "droidconke.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidRoom") {
            id = "droidconke.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidHilt") {
            id = "droidconke.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "droidconke.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFirebase") {
            id = "droidconke.android.application.firebase"
            implementationClass = "AndroidApplicationFirebaseConventionPlugin"
        }
        register("androidLibraryFirebase") {
            id = "droidconke.android.library.firebase"
            implementationClass = "AndroidLibraryFirebaseConventionPlugin"
        }
        register("androidApplication") {
            id = "droidconke.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationJacoco") {
            id = "droidconke.android.application.jacoco"
            implementationClass = "AndroidApplicationJacocoConventionPlugin"
        }
        register("androidLibraryJacoco") {
            id = "droidconke.android.library.jacoco"
            implementationClass = "AndroidLibraryJacocoConventionPlugin"
        }
        register("multiplatform") {
            id = "droidconke.multiplatform"
            implementationClass = "MultiplaftormConventionPlugin"
        }
    }
}