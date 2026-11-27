package com.classsync.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.classsync.app.ui.screens.analytics.AnalyticsScreen
import com.classsync.app.ui.screens.attendance.AttendanceScreen
import com.classsync.app.ui.screens.login.LoginScreen
import com.classsync.app.ui.screens.settings.SettingsScreen
import com.classsync.app.ui.screens.sheetlink.SheetLinkScreen
import com.classsync.app.ui.screens.tasks.AssignmentsMaterialsScreen
import com.classsync.app.ui.screens.timetable.TimetableScreen
import com.classsync.app.ui.theme.ObsidianGray
import com.classsync.app.ui.theme.PureWhite
import com.classsync.app.ui.theme.MutedSilver

@Composable
fun ClassSyncNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem.Attendance,
        BottomNavItem.Timetable,
        BottomNavItem.Assignments,
        BottomNavItem.Settings
    )

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        containerColor = com.classsync.app.ui.theme.PitchBlack,
        bottomBar      = {
            if (showBottomBar) {
                NavigationBar(containerColor = ObsidianGray) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon   = { Icon(item.icon, contentDescription = item.label) },
                            label  = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = PureWhite,
                                selectedTextColor   = PureWhite,
                                unselectedIconColor = MutedSilver,
                                unselectedTextColor = MutedSilver,
                                indicatorColor      = ObsidianGray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Login.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Screen.SheetLink.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.SheetLink.route) {
                SheetLinkScreen(
                    onDone = {
                        navController.navigate(Screen.Attendance.route) {
                            popUpTo(Screen.SheetLink.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Screen.Attendance.route) {
                            popUpTo(Screen.SheetLink.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Attendance.route) {
                AttendanceScreen(onOpenAnalytics = { studentId ->
                    navController.navigate(Screen.Analytics.withStudent(studentId))
                })
            }
            composable(Screen.Analytics.route) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLong() ?: 0L
                AnalyticsScreen(
                    studentId = studentId,
                    onBack    = { navController.popBackStack() }
                )
            }
            composable(Screen.Timetable.route) {
                TimetableScreen()
            }
            composable(Screen.AssignmentsMaterials.route) {
                AssignmentsMaterialsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onRelinkSheet = {
                        navController.navigate(Screen.SheetLink.route)
                    }
                )
            }
        }
    }
}
