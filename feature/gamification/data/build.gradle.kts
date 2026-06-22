plugins {
    alias(libs.plugins.goalguard.android.library)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.gamification.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":feature:gamification:domain"))
    implementation(libs.androidx.datastore.preferences)
}
