package com.androidandrew.sunscreen.ui.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
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
import com.androidandrew.sunscreen.ui.common.OnePaneLayout
import com.androidandrew.sunscreen.ui.common.TwoPaneLayout
import com.androidandrew.sunscreen.ui.navigation.AppDestination
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    useWideLayout: Boolean,
    onNotOnboarded: () -> Unit,
    onError: (String) -> Unit,
    onChangeSetting: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    // Uses repeatOnLifecycle under the hood. Reduces boilerplate.
    // https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
    val appState: AppState by viewModel.appState.collectAsStateWithLifecycle()
    val settingsState: AppDestination by viewModel.settingsState.collectAsStateWithLifecycle()
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

    LaunchedEffect(settingsState) {
        onChangeSetting(settingsState)
    }

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
    Box(modifier = Modifier.fillMaxSize()) {
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
        TwoPaneLayout(
            modifier = modifier,
            contentAtStart = {
                LocationBarWithState(uiState = locationBarState, onEvent = onLocationBarEvent)
                UvChartWithState(uvChartUiState)
            },
            contentAtEnd = {
                BurnTimeWithState(uiState = burnTimeUiState)
                UvTrackingWithState(uiState = uvTrackingState, onEvent = onUvTrackingEvent)
            }
        )
    } else {
        OnePaneLayout(
            modifier = modifier,
            content = {
                LocationBarWithState(uiState = locationBarState, onEvent = onLocationBarEvent)
                BurnTimeWithState(uiState = burnTimeUiState)
                UvChartWithState(uvChartUiState)
                UvTrackingWithState(uiState = uvTrackingState, onEvent = onUvTrackingEvent)
            }
        )
    }
}

@Preview(showBackground = true, device = Devices.PHONE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, device = Devices.PHONE, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun MainScreenVerticalNightAndDayPreview() {
    SunscreenTheme {
        MainScreenWithState(
            useWideLayout = false,
            locationBarState = LocationBarState("12345"),
            onLocationBarEvent = {},
            burnTimeUiState = BurnTimeUiState.Unknown,
            uvChartUiState = chartPreviewData,
            uvTrackingState = UvTrackingState.initialState,
            onUvTrackingEvent = {}
        )
    }
}

@Preview(showBackground = true, device = Devices.PHONE, widthDp = 800, heightDp = 600)
@Preview(showBackground = true, device = Devices.TABLET)
@Preview(showBackground = true, device = Devices.FOLDABLE)
@Composable
fun MainScreenWideLayoutPreview() {
    SunscreenTheme {
        MainScreenWithState(
            useWideLayout = true,
            locationBarState = LocationBarState("12345"),
            onLocationBarEvent = {},
            burnTimeUiState = BurnTimeUiState.Unknown,
            uvChartUiState = chartPreviewData,
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

private val chartPreviewData = UvChartUiState.HasData(
    data = LineDataSet(
        listOf(
            Entry(8.0f, 0.0f),
            Entry(10.0f, 5.0f),
            Entry(12.0f, 10.0f),
            Entry(14.0f, 4.0f),
            Entry(15.0f, 0.0f)
        ), ""
    ),
    xHighlight = 11.0f
)
