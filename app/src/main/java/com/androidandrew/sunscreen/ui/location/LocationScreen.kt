package com.androidandrew.sunscreen.ui.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun LocationScreen(
    onLocationValid: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = koinViewModel()
) {
    val locationBarState: LocationBarState by viewModel.locationBarState.collectAsStateWithLifecycle()
    val isLocationValid by viewModel.isLocationValid.collectAsStateWithLifecycle()

    when (isLocationValid) {
        true -> {
            LaunchedEffect(true) {
                onLocationValid()
            }
        }
        false -> {
            LocationScreenWithState(
                locationBarState = locationBarState,
                onLocationBarEvent = { viewModel.onEvent(it) },
                modifier = modifier
            )
        }
    }
}

@Composable
fun LocationScreenWithState(
    locationBarState: LocationBarState,
    onLocationBarEvent: (LocationBarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box {
            LocationBarWithState(
                uiState = locationBarState,
                onEvent = onLocationBarEvent,
                modifier = modifier.align(Alignment.Center)
            )
        }
    }
}

