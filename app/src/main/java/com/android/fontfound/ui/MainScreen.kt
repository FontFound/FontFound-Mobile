package com.android.fontfound.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.fontfound.R
import com.android.fontfound.navigation.Navigation
import com.android.fontfound.navigation.TopLevelDestination
import com.android.fontfound.ui.history.HistoryViewModel
import com.android.fontfound.ui.settings.SettingsViewModel

@Composable
fun MainScreen(settingsViewModel: SettingsViewModel, historyViewModel: HistoryViewModel) {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        navController.navigate(TopLevelDestination.History.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if(showBottomBar) BottomBar(navController = navController)
            containerColor = MaterialTheme.colorScheme.background
        }
    ) { innerPadding ->
        Navigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            settingsViewModel = settingsViewModel,
            historyViewModel = historyViewModel,
            onShowBottomBar = { showBottomBar = it }
        )
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val pages = listOf(
        TopLevelDestination.History,
        TopLevelDestination.Scan,
        TopLevelDestination.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box {
        // Navigation Bar Background
        NavigationBar {
            pages.forEachIndexed { index, page ->
                if (index != 1) {
                    AddItem(
                        page = page,
                        currentDestination = currentDestination,
                        navController = navController
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Floating Camera Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -30.dp)
        ) {
            CameraButton(navController = navController)
        }
    }
}

@Composable
fun CameraButton(navController: NavHostController) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(colorResource(id = R.color.main_color), shape = CircleShape)
            .clickable {
                navController.navigate(TopLevelDestination.Scan.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_camera),
            contentDescription = "Camera Icon",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun RowScope.AddItem(
    page: TopLevelDestination,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    NavigationBarItem(
        icon = {
            page.icon?.let {
                Icon(
                    imageVector = it,
                    modifier = Modifier.size(32.dp),
                    contentDescription = "Navigation Icon"
                )
            }.also {
                page.iconResId?.let {
                    Icon(
                        painter = painterResource(id = it),
                        modifier = Modifier.size(32.dp),
                        contentDescription = "Navigation Icon"
                    )
                }
            }
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == page.route
        } == true,
        onClick = {
            navController.navigate(page.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = LocalContentColor.current,
            unselectedIconColor = LocalContentColor.current.copy(alpha = 0.6f)
        )
    )
}