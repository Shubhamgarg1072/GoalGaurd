plugins {
    alias(libs.plugins.goalguard.android.library)
    alias(libs.plugins.goalguard.android.compose)
}

android {
    namespace = "com.time.applauncher.goalgaurd.core.presentation"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.android)
}
