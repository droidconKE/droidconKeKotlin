import com.android.build.gradle.LibraryExtension
import com.android254.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.gradle.kotlin.dsl.configure

class MultiplaftormConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")

                extensions.configure<KotlinMultiplatformExtension> {
                    androidTarget {
                        compilations.all {
                            kotlinOptions {
                                jvmTarget = "17"
                            }
                        }
                    }

                    listOf(
                        iosX64(),
                        iosArm64(),
                        iosSimulatorArm64()
                    ).forEach {
                        it.binaries.framework {
                            baseName = "shared"
                            isStatic = true
                        }
                    }
                }

                extensions.configure<LibraryExtension> {
                    compileSdk = libs.findVersion("android-compile-sdk").get().toString().toInt()
                    defaultConfig {
                        minSdk = libs.findVersion("android-min-sdk").get().toString().toInt()
                    }
                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_17
                        targetCompatibility = JavaVersion.VERSION_17
                    }
                }
            }
        }
    }
}