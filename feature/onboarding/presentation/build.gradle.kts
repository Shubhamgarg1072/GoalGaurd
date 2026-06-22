plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.onboarding.presentation"
}

dependencies {
    implementation(project(":feature:onboarding:domain"))
    implementation(project(":feature:goals:domain"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.activity.compose)
}
