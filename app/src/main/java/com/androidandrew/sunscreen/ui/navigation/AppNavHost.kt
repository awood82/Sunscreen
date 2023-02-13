package com.androidandrew.sunscreen.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.androidandrew.sunscreen.ui.clothing.ClothingScreen
import com.androidandrew.sunscreen.ui.location.LocationScreen
import com.androidandrew.sunscreen.ui.main.MainScreen
import com.androidandrew.sunscreen.ui.skintype.SkinTypeScreen
import timber.log.Timber

@Composable
fun AppNavHost(
    navController: NavHostController,
    useWideLayout: Boolean,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = AppDestination.Main.name
) {
    var returntoMainScreen by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = AppDestination.Main.name) {
            Timber.d("Loading MainScreen")
            MainScreen(
                useWideLayout = useWideLayout,
                onNotOnboarded = {
                    navController.navigate(AppDestination.Location.name)
                },
                onError = onError,
                onChangeSetting = {
                    if (it != AppDestination.Main) {
                        returntoMainScreen = true
                        navController.navigate(it.name)
                    }
                },
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
                    if (returntoMainScreen) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(AppDestination.Clothing.name)
                    }
                },
                modifier = modifier
            )
        }
        composable(route = AppDestination.Clothing.name) {
            Timber.d("Loading ClothingScreen")
            ClothingScreen(
                onContinuePressed = {
                    if (returntoMainScreen) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(AppDestination.Main.name) {
                            popUpTo(AppDestination.Main.name) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = modifier
            )
        }
    }
}