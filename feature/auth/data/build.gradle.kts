plugins {
    alias(libs.plugins.goalguard.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.time.applauncher.goalgaurd.feature.auth.data"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:domain"))
    implementation(project(":feature:auth:domain"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
