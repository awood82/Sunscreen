package com.androidandrew.sunscreen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.androidandrew.sunscreen.ui.location.LocationScreen
import com.androidandrew.sunscreen.ui.main.MainScreen
import com.androidandrew.sunscreen.ui.skintype.SkinTypeScreen
import timber.log.Timber

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onError: (String) -> Unit,
    startDestination: String = AppDestination.Main.name
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = AppDestination.Main.name) {
            Timber.d("Loading MainScreen")
            MainScreen(
                onNotOnboarded = {
                    navController.navigate(AppDestination.Location.name)
                },
                onError = onError,
                modifier = modifier
            )
        }
        composable(route = AppDestination.Location.name) {
            Timber.d("Loading LocationScreen")
            LocationScreen(
                onLocationValid = {
                    navController.navigate(AppDestination.SkinType.name)
                },
                modifier = modifier
            )
        }
        composable(route = AppDestination.SkinType.name) {
            Timber.d("Loading SkinTypeScreen")
            SkinTypeScreen(
                onSkinTypeSelected = {
                    navController.navigate(AppDestination.Main.name) {
                        popUpTo(AppDestination.Main.name) {
                            inclusive = true
                        }
                    }
                },
                modifier = modifier
            )
        }
    }
}