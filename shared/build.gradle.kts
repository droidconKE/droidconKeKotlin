plugins {
    alias(libs.plugins.droidconke.multiplatform)
}

kotlin {

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":shared:datasource"))
                api(project(":shared:data"))
                api(project(":shared:domain"))
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
    namespace = "ke.droidcon.kotlin.shared"
}