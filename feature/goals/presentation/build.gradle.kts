plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.goals.presentation"
}

dependencies {
    implementation(project(":feature:goals:domain"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
