import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.time.applauncher.goalgaurd.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "goalguard.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "goalguard.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "goalguard.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = "goalguard.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("domainModule") {
            id = "goalguard.domain.module"
            implementationClass = "DomainModuleConventionPlugin"
        }
        register("room") {
            id = "goalguard.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("koin") {
            id = "goalguard.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("ktor") {
            id = "goalguard.ktor"
            implementationClass = "KtorConventionPlugin"
        }
        register("kotlinxSerialization") {
            id = "goalguard.kotlinx.serialization"
            implementationClass = "KotlinxSerializationConventionPlugin"
        }
    }
}
