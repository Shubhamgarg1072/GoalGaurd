pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GoalGaurd"

include(":app")

// KMP shared (models/DTOs + API client) — consumed by app and backend
include(":shared")

// Backend (Ktor + Postgres) — JVM only
include(":backend")

// Core
include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:presentation")
include(":core:design-system")

// Feature: Auth (Google Sign-In + optional cloud)
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")

// Feature: Onboarding
include(":feature:onboarding:domain")
include(":feature:onboarding:data")
include(":feature:onboarding:presentation")

// Feature: Goals
include(":feature:goals:domain")
include(":feature:goals:data")
include(":feature:goals:presentation")

// Feature: Habits
include(":feature:habits:domain")
include(":feature:habits:data")
include(":feature:habits:presentation")

// Feature: Dashboard
include(":feature:dashboard:presentation")

// Feature: Focus
include(":feature:focus:domain")
include(":feature:focus:data")
include(":feature:focus:presentation")

// Feature: Insights
include(":feature:insights:presentation")

// Feature: Gamification
include(":feature:gamification:domain")
include(":feature:gamification:data")
include(":feature:gamification:presentation")

// Feature: Coach
include(":feature:coach:domain")
include(":feature:coach:presentation")

// Core: Backup
include(":core:backup")

// Feature: Backup
include(":feature:backup:presentation")

// Feature: Guard (doom-scroll detection + intervention overlay)
include(":feature:guard:domain")
include(":feature:guard:data")
include(":feature:guard:presentation")
