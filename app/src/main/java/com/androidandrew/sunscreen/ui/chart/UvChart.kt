package com.androidandrew.sunscreen.ui.chart

import android.text.format.DateFormat
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import timber.log.Timber

//@Composable
//fun UvChartWithState(
//    uiState: UvChartUiState,
//    modifier: Modifier = Modifier
//) {
//    when (uiState) {
//        is UvChartUiState.NoData -> UvChart(modifier)
//        is UvChartUiState.HasData -> UvChart(modifier, uiState.data, uiState.highlight)
//    }
//}

@Composable
fun UvChart(
    modifier: Modifier = Modifier,
    dataSet: LineDataSet? = null,
    xHighlight: Float? = null,
    chartFormatter: UvChartFormatter = UvChartFormatter(LocalContext.current),
    use24HourTime: Boolean = DateFormat.is24HourFormat(LocalContext.current)
) {
    Timber.e("lineDataSet null? ${dataSet == null}")
    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                chartFormatter.formatChart(
                    lineChart = this,
                    use24HourTime = use24HourTime
                )
            }
        },
        update = {
            if (dataSet != null) {
                chartFormatter.formatDataSet(dataSet)
                it.data = LineData(dataSet)
                xHighlight?.let { x ->
                    it.highlightValue(x, 0)
                }
                it.invalidate()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .testTag("UvChart")
    )
}

//@Preview(showBackground = true)
//@Composable
//fun UvChartNoDataPreview() {
//    MaterialTheme {
//        UvChartWithState(UvChartUiState.NoData)
//    }
//}