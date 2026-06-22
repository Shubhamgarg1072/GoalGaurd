import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class KtorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val catalog = extensions
                .getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")
            dependencies {
                "implementation"(catalog.findLibrary("ktor-client-core").get())
                "implementation"(catalog.findLibrary("ktor-client-android").get())
                "implementation"(catalog.findLibrary("ktor-client-content-negotiation").get())
                "implementation"(catalog.findLibrary("ktor-client-logging").get())
                "implementation"(catalog.findLibrary("ktor-serialization-kotlinx-json").get())
                "implementation"(catalog.findLibrary("ktor-client-auth").get())
            }
        }
    }
}
