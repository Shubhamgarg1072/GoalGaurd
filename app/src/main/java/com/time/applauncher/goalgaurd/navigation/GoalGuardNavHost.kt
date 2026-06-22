package com.time.applauncher.goalgaurd.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.time.applauncher.goalgaurd.core.designsystem.theme.BackgroundDeep
import com.time.applauncher.goalgaurd.core.designsystem.theme.Primary
import com.time.applauncher.goalgaurd.core.designsystem.theme.Surface
import com.time.applauncher.goalgaurd.core.designsystem.theme.TextMuted
import com.time.applauncher.goalgaurd.feature.dashboard.presentation.DashboardRoot
import com.time.applauncher.goalgaurd.feature.focus.presentation.FocusModeRoot
import com.time.applauncher.goalgaurd.feature.gamification.presentation.GamificationScreen
import com.time.applauncher.goalgaurd.feature.goals.presentation.GoalDetailRoot
import com.time.applauncher.goalgaurd.feature.goals.presentation.GoalsRoot
import com.time.applauncher.goalgaurd.feature.habits.presentation.HabitsRoot
import com.time.applauncher.goalgaurd.feature.insights.presentation.InsightsScreen
import com.time.applauncher.goalgaurd.feature.auth.presentation.SignInRoot
import com.time.applauncher.goalgaurd.feature.backup.presentation.BackupRoot
import com.time.applauncher.goalgaurd.feature.coach.presentation.CoachRoot
import com.time.applauncher.goalgaurd.feature.guard.presentation.GuardRoot
import com.time.applauncher.goalgaurd.feature.onboarding.presentation.LauncherPermissionsScreen
import com.time.applauncher.goalgaurd.feature.onboarding.presentation.OnboardingRoot

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, DashboardRoute),
    BottomNavItem("Goals", Icons.Default.Star, GoalsRoute),
    BottomNavItem("Habits", Icons.Default.TaskAlt, HabitsRoute),
    BottomNavItem("Insights", Icons.Default.Analytics, InsightsRoute),
    BottomNavItem("Level", Icons.Default.EmojiEvents, GamificationRoute),
)

private val topLevelRoutes = setOf(
    DashboardRoute::class,
    GoalsRoute::class,
    HabitsRoute::class,
    InsightsRoute::class,
    GamificationRoute::class,
)

@Composable
fun GoalGuardNavHost(startDestination: Any) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDest = navBackStack?.destination

    val showBottomBar = topLevelRoutes.any { currentDest?.hasRoute(it) == true }

    Scaffold(
        containerColor = BackgroundDeep,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Surface) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDest?.hasRoute(item.route::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = Primary.copy(alpha = 0.15f),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ── Onboarding ──────────────────────────────────────────────────
            composable<OnboardingRoute> {
                OnboardingRoot(
                    onNavigateToPermissions = {
                        navController.navigate(LauncherPermissionsRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    },
                )
            }

            // ── Launcher permissions setup ───────────────────────────────────
            composable<LauncherPermissionsRoute> {
                LauncherPermissionsScreen(
                    onContinue = {
                        navController.navigate(SignInRoute) {
                            popUpTo(LauncherPermissionsRoute) { inclusive = true }
                        }
                    },
                )
            }

            // ── Sign-in (optional Google account; "Skip for now" stays local) ─
            composable<SignInRoute> {
                SignInRoot(
                    onContinue = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(SignInRoute) { inclusive = true }
                        }
                    },
                )
            }

            // ── Dashboard (Home tab) ─────────────────────────────────────────
            composable<DashboardRoute> {
                DashboardRoot(
                    onNavigateToGoalDetail = { goalId ->
                        navController.navigate(GoalDetailRoute(goalId))
                    },
                    onNavigateToFocus = { navController.navigate(FocusRoute) },
                    onNavigateToGoals = {
                        navController.navigate(GoalsRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToHabits = {
                        navController.navigate(HabitsRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToInsights = {
                        navController.navigate(InsightsRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = { navController.navigate(BackupRoute) },
                )
            }

            // ── Goals (tab) ──────────────────────────────────────────────────
            composable<GoalsRoute> {
                GoalsRoot(
                    onNavigateToDetail = { goalId ->
                        navController.navigate(GoalDetailRoute(goalId))
                    },
                )
            }

            // ── Goal Detail (stack) ──────────────────────────────────────────
            composable<GoalDetailRoute> { backStack ->
                val route = backStack.toRoute<GoalDetailRoute>()
                GoalDetailRoot(
                    goalId = route.goalId,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ── Habits (tab) ─────────────────────────────────────────────────
            composable<HabitsRoute> {
                HabitsRoot()
            }

            // ── Focus Mode (stack, launched from Dashboard) ───────────────────
            composable<FocusRoute> {
                FocusModeRoot(
                    onNavigateBack = {
                        // Session complete/abandon: pop back to wherever we came from
                        navController.popBackStack()
                    },
                )
            }

            // ── Insights (tab) ───────────────────────────────────────────────
            composable<InsightsRoute> {
                InsightsScreen()
            }

            // ── Gamification / Level (tab) ───────────────────────────────────
            composable<GamificationRoute> {
                GamificationScreen()
            }

            // ── Coach / Evening Summary (stack) ──────────────────────────────
            composable<CoachRoute> {
                CoachRoot(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            // ── Backup & Restore (stack, reachable from Dashboard settings) ──
            composable<BackupRoute> {
                BackupRoot(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGuard = { navController.navigate(GuardRoute) },
                )
            }

            // ── Doom-scroll Guard settings (stack, reachable from settings) ──
            composable<GuardRoute> {
                GuardRoot(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
