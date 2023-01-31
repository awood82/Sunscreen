package com.androidandrew.sunscreen.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
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
import com.androidandrew.sunscreen.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    useWideLayout: Boolean,
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
    val forecastState: ForecastState by viewModel.forecastState.collectAsStateWithLifecycle()

    onError(
        when (val state = forecastState) {
            is ForecastState.Error -> state.message
            else -> ""
        }
    )

    when (appState) {
        AppState.Loading -> {}
        AppState.NotOnboarded -> {
            LaunchedEffect(Unit) {
                onNotOnboarded()
            }
        }
        AppState.Onboarded -> {
            MainScreenWithState(
                useWideLayout = useWideLayout,
                locationBarState = locationBarState,
                onLocationBarEvent = { viewModel.onLocationBarEvent(it) },
                burnTimeUiState = burnTimeUiState,
                uvChartUiState = uvChartUiState,
                uvTrackingState = uvTrackingState,
                onUvTrackingEvent = { viewModel.onUvTrackingEvent(it) },
                modifier = modifier
            )
            if (forecastState is ForecastState.Loading) {
                LoadingOverlay(modifier)
            }
        }
    }
}

@Composable
private fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = modifier
                .align(Alignment.Center)
                .testTag(stringResource(R.string.loading)),
        )
    }
}


@Composable
private fun MainScreenWithState(
    useWideLayout: Boolean,
    locationBarState: LocationBarState,
    onLocationBarEvent: (LocationBarEvent) -> Unit,
    burnTimeUiState: BurnTimeUiState,
    uvChartUiState: UvChartUiState,
    uvTrackingState: UvTrackingState,
    onUvTrackingEvent: (UvTrackingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (useWideLayout) {
        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                LocationBarWithState(uiState = locationBarState, onEvent = onLocationBarEvent)
                UvChartWithState(uvChartUiState)
            }
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                BurnTimeWithState(uiState = burnTimeUiState)
                UvTrackingWithState(uiState = uvTrackingState, onEvent = onUvTrackingEvent)
            }
        }
    } else {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            LocationBarWithState(uiState = locationBarState, onEvent = onLocationBarEvent)
            BurnTimeWithState(uiState = burnTimeUiState)
            UvChartWithState(uvChartUiState)
            UvTrackingWithState(uiState = uvTrackingState, onEvent = onUvTrackingEvent)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenVerticalPreview() {
    SunscreenTheme {
        MainScreenWithState(
            useWideLayout = false,
            locationBarState = LocationBarState("12345"),
            onLocationBarEvent = {},
            burnTimeUiState = BurnTimeUiState.Unknown,
            uvChartUiState = UvChartUiState.NoData,
            uvTrackingState = UvTrackingState.initialState,
            onUvTrackingEvent = {}
        )
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
fun MainScreenHorizontalPreview() {
    SunscreenTheme {
        MainScreenWithState(
            useWideLayout = true,
            locationBarState = LocationBarState("12345"),
            onLocationBarEvent = {},
            burnTimeUiState = BurnTimeUiState.Unknown,
            uvChartUiState = UvChartUiState.NoData,
            uvTrackingState = UvTrackingState.initialState,
            onUvTrackingEvent = {}
        )
    }
}

@Preview(showBackground = true, device = Devices.FOLDABLE)
@Composable
fun MainScreenFoldablePreview() {
    SunscreenTheme {
        MainScreenWithState(
            useWideLayout = true,
            locationBarState = LocationBarState("12345"),
            onLocationBarEvent = {},
            burnTimeUiState = BurnTimeUiState.Unknown,
            uvChartUiState = UvChartUiState.NoData,
            uvTrackingState = UvTrackingState.initialState,
            onUvTrackingEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingPreview() {
    SunscreenTheme {
        LoadingOverlay()
    }
}
