import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val catalog = extensions
                .getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")

            extensions.configure<LibraryExtension> {
                buildFeatures { compose = true }
            }

            dependencies {
                val bom = catalog.findLibrary("androidx-compose-bom").get()
                "implementation"(platform(bom))
                "implementation"(catalog.findLibrary("androidx-compose-ui").get())
                "implementation"(catalog.findLibrary("androidx-compose-ui-graphics").get())
                "implementation"(catalog.findLibrary("androidx-compose-ui-tooling-preview").get())
                "implementation"(catalog.findLibrary("androidx-compose-material3").get())
                "implementation"(catalog.findLibrary("androidx-compose-material-icons-extended").get())
                "implementation"(catalog.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(catalog.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "debugImplementation"(catalog.findLibrary("androidx-compose-ui-tooling").get())
                "debugImplementation"(catalog.findLibrary("androidx-compose-ui-test-manifest").get())
            }
        }
    }
}
