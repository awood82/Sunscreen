package com.androidandrew.sunscreen.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    useWideLayout: Boolean,
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
//    val backStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf("") }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .semantics { testTagsAsResourceId = true }
        ) {
            Timber.d("Loading AppNavHost")
            AppNavHost(
                navController = navController,
                useWideLayout = useWideLayout,
                onError = { snackbarMessage = it }
            )
        }
    }
}