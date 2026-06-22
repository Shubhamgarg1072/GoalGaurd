import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            val catalog = extensions
                .getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")
            dependencies {
                "implementation"(catalog.findLibrary("androidx-room-runtime").get())
                "implementation"(catalog.findLibrary("androidx-room-ktx").get())
                "ksp"(catalog.findLibrary("androidx-room-compiler").get())
            }
        }
    }
}
