plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.focus.presentation"
}

dependencies {
    implementation(project(":feature:focus:domain"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
