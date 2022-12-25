package com.androidandrew.sunscreen.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import com.androidandrew.sunscreen.ui.tracking.UvTrackingEvent
import com.androidandrew.sunscreen.ui.tracking.UvTrackingState
import com.androidandrew.sunscreen.ui.tracking.UvTrackingWithState
import org.koin.androidx.compose.get

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = get(),
    modifier: Modifier = Modifier.semantics { testTagsAsResourceId = true }
) {
    // Uses repeatOnLifecycle under the hood. Reduces boilerplate.
    // https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
    val burnTimeUiState: BurnTimeUiState by viewModel.burnTimeUiState.collectAsStateWithLifecycle()
    val uvChartUiState: UvChartUiState by viewModel.uvChartUiState.collectAsStateWithLifecycle()
    val uvTrackingState: UvTrackingState by viewModel.uvTrackingState.collectAsStateWithLifecycle()
    val locationBarState: LocationBarState by viewModel.locationBarState.collectAsStateWithLifecycle()

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
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
    MaterialTheme {
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
