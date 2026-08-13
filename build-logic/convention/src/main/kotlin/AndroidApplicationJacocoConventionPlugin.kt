import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android254.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("jacoco")
            // ApplicationExtension is the public DSL type. BaseAppModuleExtension lives in
            // com.android.build.gradle.internal and is not ours to depend on.
            val androidExtension = extensions.getByType<ApplicationExtension>()

            // Debug only. Coverage instrumentation forces the build type debuggable, and a
            // debuggable build type silently turns off every R8 optimisation and all
            // obfuscation — so enabling this on `release` shipped an unminified APK.
            androidExtension.buildTypes.getByName("debug") {
                enableAndroidTestCoverage = true
                enableUnitTestCoverage = true
            }

            configureJacoco(extensions.getByType<ApplicationAndroidComponentsExtension>())
        }
    }
}