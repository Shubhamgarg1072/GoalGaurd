plugins {
    alias(libs.plugins.goalguard.android.feature)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.vault.presentation"
}

dependencies {
    implementation(project(":feature:vault:domain"))
    implementation(libs.kotlinx.coroutines.android)
}
