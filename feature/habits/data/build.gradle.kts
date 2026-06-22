plugins {
    alias(libs.plugins.goalguard.android.library)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.habits.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":feature:habits:domain"))
    implementation(libs.kotlinx.coroutines.android)
}
