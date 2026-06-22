plugins {
    alias(libs.plugins.goalguard.android.library)
    alias(libs.plugins.goalguard.android.compose)
}

android {
    namespace = "com.time.applauncher.goalgaurd.core.designsystem"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
