plugins {
    alias(libs.plugins.goalguard.android.library)
    alias(libs.plugins.goalguard.room)
}

android {
    namespace = "com.time.applauncher.goalgaurd.core.database"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.android)
}
