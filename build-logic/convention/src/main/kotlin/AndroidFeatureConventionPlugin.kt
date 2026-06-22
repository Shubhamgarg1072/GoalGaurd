import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("goalguard.android.library")
            pluginManager.apply("goalguard.android.compose")
            pluginManager.apply("goalguard.koin")

            dependencies {
                "implementation"(project(":core:presentation"))
                "implementation"(project(":core:design-system"))
                "implementation"(project(":core:domain"))
            }
        }
    }
}
