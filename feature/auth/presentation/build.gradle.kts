plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.auth.presentation"
}

dependencies {
    implementation(project(":feature:auth:domain"))
    implementation(project(":feature:auth:data"))
    implementation(libs.kotlinx.coroutines.android)
}
