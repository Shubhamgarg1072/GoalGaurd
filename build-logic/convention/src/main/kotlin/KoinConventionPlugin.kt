import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val catalog = extensions
                .getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")
            dependencies {
                "implementation"(catalog.findLibrary("koin-android").get())
                "implementation"(catalog.findLibrary("koin-androidx-compose").get())
            }
        }
    }
}
