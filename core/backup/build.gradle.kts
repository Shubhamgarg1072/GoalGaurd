plugins {
    alias(libs.plugins.goalguard.android.library)
    alias(libs.plugins.goalguard.kotlinx.serialization)
}

android {
    namespace = "com.time.applauncher.goalgaurd.core.backup"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:crypto"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.ktx)
}
