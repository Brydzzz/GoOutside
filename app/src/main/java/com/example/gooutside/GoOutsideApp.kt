package com.example.gooutside

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gooutside.ui.navigation.AppNavBar
import com.example.gooutside.ui.navigation.AppNavHost
import com.example.gooutside.ui.navigation.MainDestination
import com.example.gooutside.ui.theme.GoOutsideTheme

@Composable
fun GoOutsideApp(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val routesWithoutBottomBar = listOf(MainDestination.PhotoMode.route)
    val shouldShowBottomBar = currentRoute !in routesWithoutBottomBar

    val scaffoldColor = when (currentRoute) {
        MainDestination.PhotoMode.route -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.background
    }

    Scaffold(
        containerColor = scaffoldColor,
        bottomBar = {
            if (shouldShowBottomBar) {
                AppNavBar(currentRoute, onNavigate = {
                    navController.navigate(it) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
        }
    ) { innerPadding ->
        AppNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Preview(showBackground = true)
@Composable
fun GoOutsideAppPreview() {
    GoOutsideTheme { GoOutsideApp() }
}