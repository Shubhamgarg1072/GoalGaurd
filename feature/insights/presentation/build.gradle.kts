plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.insights.presentation"
}

dependencies {
    implementation(project(":feature:habits:domain"))
    implementation(project(":feature:goals:domain"))
    implementation(libs.androidx.navigation.compose)
}
