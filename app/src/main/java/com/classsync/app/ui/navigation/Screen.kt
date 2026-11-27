package com.classsync.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login               : Screen("login")
    object SheetLink           : Screen("sheet_link")
    object Attendance          : Screen("attendance")
    object Analytics           : Screen("analytics/{studentId}") {
        fun withStudent(id: Long) = "analytics/$id"
    }
    object Timetable           : Screen("timetable")
    object AssignmentsMaterials : Screen("assignments_materials")
    object Settings            : Screen("settings")
}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Attendance  : BottomNavItem(Screen.Attendance.route,             "Attendance", Icons.Default.People)
    object Timetable   : BottomNavItem(Screen.Timetable.route,              "Timetable",  Icons.Default.CalendarMonth)
    object Assignments : BottomNavItem(Screen.AssignmentsMaterials.route,   "Tasks",      Icons.Default.Assignment)
    object Settings    : BottomNavItem(Screen.Settings.route,               "Settings",   Icons.Default.Settings)
}
