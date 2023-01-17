package com.androidandrew.sunscreen.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.ui.burntime.BurnTimeUiState
import com.androidandrew.sunscreen.ui.burntime.BurnTimeWithState
import com.androidandrew.sunscreen.ui.chart.UvChartUiState
import com.androidandrew.sunscreen.ui.chart.UvChartWithState
import com.androidandrew.sunscreen.ui.location.LocationBarEvent
import com.androidandrew.sunscreen.ui.location.LocationBarState
import com.androidandrew.sunscreen.ui.location.LocationBarWithState
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
import com.androidandrew.sunscreen.ui.tracking.UvTrackingEvent
import com.androidandrew.sunscreen.ui.tracking.UvTrackingState
import com.androidandrew.sunscreen.ui.tracking.UvTrackingWithState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    onNotOnboarded: () -> Unit,
    onError: (String) -> Unit
) {
    // Uses repeatOnLifecycle under the hood. Reduces boilerplate.
    // https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
    val appState: AppState by viewModel.appState.collectAsStateWithLifecycle()
    val burnTimeUiState: BurnTimeUiState by viewModel.burnTimeUiState.collectAsStateWithLifecycle()
    val uvChartUiState: UvChartUiState by viewModel.uvChartUiState.collectAsStateWithLifecycle()
    val uvTrackingState: UvTrackingState by viewModel.uvTrackingState.collectAsStateWithLifecycle()
    val locationBarState: LocationBarState by viewModel.locationBarState.collectAsStateWithLifecycle()
    val errorMessage: String by viewModel.errorMessage.collectAsStateWithLifecycle()

    onError(errorMessage)

    when (appState) {
        AppState.Loading -> {}
        AppState.NotOnboarded -> {
            LaunchedEffect(Unit) {
                onNotOnboarded()
            }
        }
        AppState.Onboarded -> {
            MainScreenWithState(
                locationBarState = locationBarState,
                onLocationBarEvent = { viewModel.onLocationBarEvent(it) },
                burnTimeUiState = burnTimeUiState,
                uvChartUiState = uvChartUiState,
                uvTrackingState = uvTrackingState,
                onUvTrackingEvent = { viewModel.onUvTrackingEvent(it) },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun MainScreenWithState(
    locationBarState: LocationBarState,
    onLocationBarEvent: (LocationBarEvent) -> Unit,
    burnTimeUiState: BurnTimeUiState,
    uvChartUiState: UvChartUiState,
    uvTrackingState: UvTrackingState,
    onUvTrackingEvent: (UvTrackingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        LocationBarWithState(uiState = locationBarState, onEvent = onLocationBarEvent)
        BurnTimeWithState(uiState = burnTimeUiState)
        UvChartWithState(uvChartUiState)
        UvTrackingWithState(uiState = uvTrackingState, onEvent = onUvTrackingEvent)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SunscreenTheme {
        MainScreenWithState(
            locationBarState = LocationBarState("12345"),
            onLocationBarEvent = {},
            burnTimeUiState = BurnTimeUiState.Unknown,
            uvChartUiState = UvChartUiState.NoData,
            uvTrackingState = UvTrackingState.initialState,
            onUvTrackingEvent = {}
        )
    }
}
