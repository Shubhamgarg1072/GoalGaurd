plugins {
    alias(libs.plugins.goalguard.android.library)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.guard.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":feature:guard:domain"))
    // Provides the ScreenTimeProvider seam that Phase 1 stubbed with NoopScreenTimeProvider.
    implementation(project(":feature:coach:presentation"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
