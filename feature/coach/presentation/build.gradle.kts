plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.coach.presentation"
}

dependencies {
    implementation(project(":feature:coach:domain"))
    implementation(project(":feature:goals:domain"))
    implementation(project(":feature:habits:domain"))
    implementation(project(":feature:focus:domain"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
