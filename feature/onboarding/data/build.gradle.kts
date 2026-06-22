plugins {
    alias(libs.plugins.goalguard.android.library)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.onboarding.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":feature:onboarding:domain"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
