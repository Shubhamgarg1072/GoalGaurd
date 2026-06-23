plugins {
    alias(libs.plugins.goalguard.domain.module)
}

dependencies {
    implementation(project(":core:crypto"))
    implementation(libs.kotlinx.coroutines.core)
}
