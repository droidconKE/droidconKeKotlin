plugins {
    alias(libs.plugins.droidconke.multiplatform)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                // implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "ke.droidcon.kotlin.shared.domain"
}