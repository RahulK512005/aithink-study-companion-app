package com.aithink.studycompanion.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aithink.studycompanion.data.local.PreferencesManager
import com.aithink.studycompanion.ui.screens.login.LoginScreen
import com.aithink.studycompanion.ui.screens.dashboard.DashboardScreen
import com.aithink.studycompanion.ui.screens.dashboard.DashboardViewModel
import com.aithink.studycompanion.ui.screens.subjects.SubjectsScreen as SubjectsScreenNew
import com.aithink.studycompanion.ui.screens.history.HistoryScreen
import com.aithink.studycompanion.ui.screens.profile.ProfileScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(preferencesManager: PreferencesManager) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Create shared ViewModel for Dashboard and History
    val dashboardViewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(Unit) {
        scope.launch {
            preferencesManager.getUserProfile().collect { profile ->
                startDestination = if (profile == null) {
                    Screen.Login.route
                } else {
                    Screen.Home.route
                }
            }
        }
    }

    if (startDestination != null) {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Show bottom bar only on main screens
                if (currentDestination?.route in listOf(
                        Screen.Home.route,
                        Screen.Dashboard.route,
                        Screen.Subjects.route,
                        Screen.History.route,
                        Screen.Profile.route
                    )
                ) {
                    NavigationBar {
                        val items = listOf(
                            Triple(Screen.Home.route, Icons.Default.Home, "Home"),
                            Triple(Screen.Dashboard.route, Icons.Default.Dashboard, "Dashboard"),
                            Triple(Screen.Subjects.route, Icons.Default.Book, "Subjects"),
                            Triple(Screen.History.route, Icons.Default.History, "History"),
                            Triple(Screen.Profile.route, Icons.Default.Person, "Profile"),
                        )

                        items.forEach { (route, icon, label) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                                onClick = {
                                    navController.navigate(route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = startDestination!!,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        preferencesManager = preferencesManager,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(navController = navController)
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(dashboardViewModel)
                }

                composable(Screen.Subjects.route) {
                    SubjectsScreenNew()
                }

                composable(Screen.History.route) {
                    HistoryScreen(dashboardViewModel = dashboardViewModel)
                }

                composable(Screen.Profile.route) {
                    ProfileScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: androidx.navigation.NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎓 AiThink", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("AI Study Companion", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { navController.navigate(Screen.Dashboard.route) }) {
                Text("Start Learning")
            }
        }
    }
}


