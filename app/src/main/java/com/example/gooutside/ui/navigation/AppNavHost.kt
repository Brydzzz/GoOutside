package com.example.gooutside.ui.navigation

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gooutside.ui.diary.DiaryScreen
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
                navigateToDiaryPage = { navController.navigate(SectionDestination.Diary.route) },
                navigateToDiaryEntry = { /* TODO */ },
            )
        }
        composable(
            route = MainDestination.PhotoMode.route,
            enterTransition = {
                slideInVertically(initialOffsetY = { it })
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { it })
            },
        )
        {
            PhotoModeScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(route = MainDestination.Settings.route) {
            SettingsScreen()
        }
        composable(route = SectionDestination.Diary.route) {
            DiaryScreen(
                navigateToHome = { navController.navigate(MainDestination.Home.route) },
                onDiaryEntryClick = { /* TODO */ },
            )
        }
        composable(route = SectionDestination.Stats.route) {
            // TODO: Stats screen
        }

    }
}