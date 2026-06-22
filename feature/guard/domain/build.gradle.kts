plugins {
    alias(libs.plugins.goalguard.domain.module)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.assertk)
}
