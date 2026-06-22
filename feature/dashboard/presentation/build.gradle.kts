plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.dashboard.presentation"
}

dependencies {
    implementation(project(":feature:goals:domain"))
    implementation(project(":feature:habits:domain"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
