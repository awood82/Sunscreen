package com.androidandrew.sunscreen.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidandrew.sunscreen.ui.burntime.BurnTimeUiState
import com.androidandrew.sunscreen.ui.chart.UvChart
import com.androidandrew.sunscreen.ui.burntime.BurnTimeWithState
import com.androidandrew.sunscreen.ui.chart.UvChartUiState
import org.koin.androidx.compose.get

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = get(),
    modifier: Modifier = Modifier
) {
    // Uses repeatOnLifecycle under the hood. Reduces boilerplate.
    // https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
    val burnTimeUiState: BurnTimeUiState by viewModel.burnTimeUiState.collectAsStateWithLifecycle()
    val uvChartUiState: UvChartUiState by viewModel.uvChartUiState.collectAsStateWithLifecycle()

    MainScreenWithState(
        burnTimeUiState = burnTimeUiState,
        uvChartUiState = uvChartUiState,
        modifier = modifier
    )
}

@Composable
private fun MainScreenWithState(
    burnTimeUiState: BurnTimeUiState,
    uvChartUiState: UvChartUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BurnTimeWithState(uiState = burnTimeUiState)
        if (uvChartUiState is UvChartUiState.HasData) {
            UvChart(dataSet = uvChartUiState.data, xHighlight = uvChartUiState.xHighlight)
        } else {
            UvChart(dataSet = null, xHighlight = null)
        }
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