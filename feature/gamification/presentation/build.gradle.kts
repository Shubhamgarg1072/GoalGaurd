plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.gamification.presentation"
}

dependencies {
    implementation(project(":feature:gamification:domain"))
    implementation(libs.androidx.navigation.compose)
}
