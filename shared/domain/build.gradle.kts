plugins {
    alias(libs.plugins.droidconke.multiplatform)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {

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
    compileSdk = 34
}
