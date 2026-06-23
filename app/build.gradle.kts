plugins {
    alias(libs.plugins.goalguard.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.time.applauncher.goalgaurd"
    defaultConfig {
        applicationId = "com.time.applauncher.goalgaurd"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Cloud backend base URL. 10.0.2.2 = the host machine from the Android emulator.
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
        // OAuth *web* client ID for Google Sign-In. Leave empty until you create one in
        // Google Cloud Console (the backend can run with insecure dev verification meanwhile).
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"\"")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)
    implementation(libs.androidx.core.splashscreen)

    // KMP shared (DTOs + API client) and cloud auth
    implementation(project(":shared"))
    implementation(project(":feature:auth:domain"))
    implementation(project(":feature:auth:data"))
    implementation(project(":feature:auth:presentation"))
    implementation(libs.kotlinx.datetime)

    // Core modules
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(project(":core:crypto"))

    // Vault (end-to-end encryption)
    implementation(project(":feature:vault:domain"))
    implementation(project(":feature:vault:data"))
    implementation(project(":feature:vault:presentation"))

    // Feature modules
    implementation(project(":feature:onboarding:domain"))
    implementation(project(":feature:onboarding:data"))
    implementation(project(":feature:onboarding:presentation"))
    implementation(project(":feature:goals:domain"))
    implementation(project(":feature:goals:data"))
    implementation(project(":feature:goals:presentation"))
    implementation(project(":feature:habits:domain"))
    implementation(project(":feature:habits:data"))
    implementation(project(":feature:habits:presentation"))
    implementation(project(":feature:dashboard:presentation"))
    implementation(project(":feature:focus:domain"))
    implementation(project(":feature:focus:data"))
    implementation(project(":feature:focus:presentation"))
    implementation(project(":feature:insights:presentation"))
    implementation(project(":feature:gamification:domain"))
    implementation(project(":feature:gamification:data"))
    implementation(project(":feature:gamification:presentation"))
    implementation(project(":feature:coach:domain"))
    implementation(project(":feature:coach:presentation"))
    implementation(project(":core:backup"))
    implementation(project(":feature:backup:presentation"))
    implementation(project(":feature:guard:domain"))
    implementation(project(":feature:guard:data"))
    implementation(project(":feature:guard:presentation"))

    // AndroidX
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room (entities & DAOs compiled in :core:database, only need runtime here)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // SQLCipher: encrypts the local Room database at rest, keyed by the vault DEK.
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
