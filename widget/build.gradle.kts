plugins {
    alias(libs.plugins.droidconke.quality)
    alias(libs.plugins.droidconke.android.library)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "ke.droidcon.kotlin.widget"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
}
