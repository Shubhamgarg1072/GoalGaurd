plugins {
    alias(libs.plugins.goalguard.domain.module)
}

dependencies {
    implementation(project(":feature:goals:domain"))
    implementation(project(":feature:habits:domain"))
    implementation(project(":feature:focus:domain"))
    testImplementation(libs.junit)
    testImplementation(libs.assertk)
    testImplementation(libs.kotlinx.coroutines.test)
}
