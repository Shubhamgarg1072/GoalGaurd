plugins {
    alias(libs.plugins.goalguard.android.library)
    alias(libs.plugins.goalguard.ktor)
    alias(libs.plugins.goalguard.kotlinx.serialization)
}

android {
    namespace = "com.time.applauncher.goalgaurd.core.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
