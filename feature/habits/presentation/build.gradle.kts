plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.habits.presentation"
}

dependencies {
    implementation(project(":feature:habits:domain"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
