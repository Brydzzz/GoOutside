package com.example.gooutside.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gooutside.ui.theme.GoOutsideTheme

@Composable
fun AppNavBar(currentRoute: String?, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar {
        MainDestination.entries.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        painter = if (currentRoute == destination.route) painterResource(id = destination.iconFilled) else painterResource(
                            id = destination.iconOutline
                        ),
                        contentDescription = stringResource(id = destination.contentDescription)
                    )
                },
                label = { Text(text = stringResource(id = destination.label)) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavBarPreview() {
    GoOutsideTheme {
        AppNavBar(currentRoute = MainDestination.Home.route, onNavigate = {})
    }
}