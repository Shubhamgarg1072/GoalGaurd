package com.time.applauncher.goalgaurd.navigation

import kotlinx.serialization.Serializable

@Serializable object OnboardingRoute
@Serializable object LauncherPermissionsRoute
@Serializable object SignInRoute
@Serializable object DashboardRoute
@Serializable object GoalsRoute
@Serializable data class GoalDetailRoute(val goalId: String)
@Serializable object HabitsRoute
@Serializable object FocusRoute
@Serializable object InsightsRoute
@Serializable object GamificationRoute
@Serializable object CoachRoute
@Serializable object BackupRoute
@Serializable object GuardRoute
