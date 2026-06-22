plugins {
    alias(libs.plugins.goalguard.android.library)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.focus.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":feature:focus:domain"))
    implementation(libs.kotlinx.coroutines.android)
}
