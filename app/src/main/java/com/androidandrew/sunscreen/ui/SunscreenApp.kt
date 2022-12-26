package com.androidandrew.sunscreen.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.androidandrew.sunscreen.ui.navigation.AppNavHost
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SunscreenApp(
    modifier: Modifier = Modifier.semantics { testTagsAsResourceId = true },
    navController: NavHostController = rememberNavController()
) {
//    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
//        topBar = {
//            SunscreenTopBar(
//                currentScreen = AppDestination.valueOf(
//                    backStackEntry?.destination?.route //?.substringBefore('/')
//                        ?: AppDestination.Main.name
//                ),
//                canNavigateUp = navController.previousBackStackEntry != null,
//                navigateUp = { navController.navigateUp() }
//            )
//        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Timber.d("Loading AppNavHost")
            AppNavHost(navController = navController)
        }
    }
}