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
import com.androidandrew.sunscreen.ui.burntime.BurnTimeState
import com.androidandrew.sunscreen.ui.burntime.BurnTimeWithState
import com.androidandrew.sunscreen.ui.chart.UvChartState
import com.androidandrew.sunscreen.ui.chart.UvChartWithState
import com.androidandrew.sunscreen.ui.tracking.UvTrackingEvent
import com.androidandrew.sunscreen.ui.tracking.UvTrackingState
import com.androidandrew.sunscreen.ui.tracking.UvTrackingWithState
import org.koin.androidx.compose.get
import timber.log.Timber

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = get(),
    modifier: Modifier = Modifier.semantics { testTagsAsResourceId = true }
) {
    // Uses repeatOnLifecycle under the hood. Reduces boilerplate.
    // https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
    val burnTimeState: BurnTimeState by viewModel.burnTimeState.collectAsStateWithLifecycle()
    val uvChartState: UvChartState by viewModel.uvChartState.collectAsStateWithLifecycle()
    val uvTrackingState: UvTrackingState by viewModel.uvTrackingState.collectAsStateWithLifecycle()

    Timber.e("Recomposing MainScreen")
    MainScreenWithState(
        burnTimeState = burnTimeState,
        uvChartState = uvChartState,
        uvTrackingState = uvTrackingState,
        onUvTrackingEvent = { viewModel.onUvTrackingEvent(it) },
        modifier = modifier
    )
}

@Composable
private fun MainScreenWithState(
    burnTimeState: BurnTimeState,
    uvChartState: UvChartState,
    uvTrackingState: UvTrackingState,
    onUvTrackingEvent: (UvTrackingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Timber.e("Recomposing MainScreenWithState")
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BurnTimeWithState(uiState = burnTimeState)
        UvChartWithState(uiState = uvChartState)
        UvTrackingWithState(uiState = uvTrackingState, onEvent = onUvTrackingEvent)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreenWithState(
            burnTimeState = BurnTimeState.Unknown,
            uvChartState = UvChartState.NoData,
            uvTrackingState = UvTrackingState.initialState,
            onUvTrackingEvent = {}
        )
    }
}
