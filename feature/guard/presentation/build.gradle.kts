plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.guard.presentation"
}

dependencies {
    implementation(project(":feature:guard:domain"))
    implementation(project(":feature:goals:domain"))
    implementation(project(":feature:habits:domain"))

    // Overlay hosts Compose in a non-Activity window → needs view-tree owners wired manually.
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.savedstate)
    implementation(libs.kotlinx.coroutines.android)
}
