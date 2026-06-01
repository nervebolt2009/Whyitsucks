package com.apexmusic.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.apexmusic.ui.screens.HomeScreen
import com.apexmusic.ui.screens.LibraryScreen
import com.apexmusic.ui.screens.PlayerScreen
import com.apexmusic.ui.screens.SearchScreen
import com.apexmusic.ui.theme.ApexBlack
import com.apexmusic.viewmodel.MusicViewModel

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val PLAYER = "player"
    const val LIBRARY = "library"
}

@Composable
fun AppNavigation(musicViewModel: MusicViewModel = viewModel()) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = Modifier.background(ApexBlack)
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = musicViewModel,
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToPlayer = { navController.navigate(Routes.PLAYER) },
                onNavigateToLibrary = { navController.navigate(Routes.LIBRARY) }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = musicViewModel,
                onNavigateToPlayer = {
                    navController.navigate(Routes.PLAYER) {
                        // Don't stack multiple player screens
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.PLAYER) {
            PlayerScreen(viewModel = musicViewModel)
        }

        composable(Routes.LIBRARY) {
            LibraryScreen(
                viewModel = musicViewModel,
                onNavigateToPlayer = {
                    navController.navigate(Routes.PLAYER) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
