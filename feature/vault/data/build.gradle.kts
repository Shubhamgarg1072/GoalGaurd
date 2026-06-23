plugins {
    alias(libs.plugins.goalguard.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.vault.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:crypto"))
    implementation(project(":feature:vault:domain"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
