package com.androidandrew.sunscreen.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.ui.chart.UvChart
import com.androidandrew.sunscreen.ui.main.burntime.BurnTimeWithState
import com.github.mikephil.charting.data.LineDataSet
import org.koin.androidx.compose.get

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = get(),   //TODO: BurnTimeViewModel
    modifier: Modifier = Modifier
) {
    // Uses repeatOnLifecycle under the hood. Reduces boilerplate.
    // https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
    val burnTimeUiState: BurnTimeUiState by viewModel.burnTimeUiState.collectAsStateWithLifecycle()
//    val uvChartUiState: UvChartUiState by viewModel.uvChartUiState.collectAsStateWithLifecycle()
    val chartData by viewModel.chartData.collectAsStateWithLifecycle(initialValue = null, lifecycle = LocalLifecycleOwner.current.lifecycle)
    val highlight by viewModel.chartHighlightValue.collectAsStateWithLifecycle()

    MainScreenWithState(
        burnTimeUiState = burnTimeUiState,
//        uvChartUiState = uvChartUiState,
        chartData = chartData,
        highlight = highlight,
        modifier = modifier
    )
}

@Composable
private fun MainScreenWithState(
    burnTimeUiState: BurnTimeUiState,
//    uvChartUiState: UvChartUiState,
    chartData: LineDataSet?,
    highlight: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BurnTimeWithState(uiState = burnTimeUiState)
//        UvChartWithState(uiState = uvChartUiState)
        UvChart(dataSet = chartData, xHighlight = highlight)
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    MaterialTheme {
//        MainScreenWithState(
//            burnTimeUiState = BurnTimeUiState.Unknown,
//            uvChartUiState = UvChartUiState.NoData
//        )
//    }
//}
