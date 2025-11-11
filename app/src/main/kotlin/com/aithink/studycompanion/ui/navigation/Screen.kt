package com.aithink.studycompanion.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object Subjects : Screen("subjects")
    object History : Screen("history")
    object Profile : Screen("profile")
}
