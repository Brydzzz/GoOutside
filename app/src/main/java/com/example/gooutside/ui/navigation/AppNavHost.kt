package com.example.gooutside.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gooutside.ui.home.HomeScreen
import com.example.gooutside.ui.photo.PhotoModeScreen
import com.example.gooutside.ui.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MainDestination.Home.route,
        modifier = modifier
    ) {
        composable(route = MainDestination.Home.route) {
            HomeScreen(
                navigateToStatsPage = { /* TODO */ },
                navigateToDiaryPage = { /* TODO */ },
                navigateToDiaryEntry = { /* TODO */ },
            )
        }
        composable(route = MainDestination.PhotoMode.route) {
            PhotoModeScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = MainDestination.Settings.route) {
            SettingsScreen()
        }
    }
}