plugins {
    alias(libs.plugins.droidconke.multiplatform)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared:datasource"))
                implementation(project(":shared:domain"))
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
    namespace = "ke.droidcon.kotlin.shared.data"
}
