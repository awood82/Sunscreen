package com.androidandrew.sunscreen.ui.chart

import android.text.format.DateFormat
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import timber.log.Timber

@Composable
fun UvChartWithState(
    uiState: UvChartState,
    modifier: Modifier = Modifier
) {
    Timber.e("Recomposing UvChartWithState")
    when (uiState) {
        is UvChartState.NoData -> UvChart(modifier)
        is UvChartState.HasData -> UvChart(modifier, uiState.data, uiState.highlight)
    }
}

@Composable
fun UvChart(
    modifier: Modifier = Modifier,
    dataSet: LineDataSet? = null,
    xHighlight: Float? = null,
    chartFormatter: UvChartFormatter = UvChartFormatter(LocalContext.current),
    use24HourTime: Boolean = DateFormat.is24HourFormat(LocalContext.current)
) {
    if (dataSet != null && dataSet.entryCount != 0) {
        Text("Please just display ${dataSet.entryCount} values")
    }
    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                chartFormatter.formatChart(
                    lineChart = this,
                    use24HourTime = use24HourTime
                )
            }
        },
        update = { lineChart ->
            Timber.e("Updating LineChart")
            lineChart.refreshDrawableState()
            if (dataSet != null) {
                chartFormatter.formatDataSet(dataSet)
                lineChart.notifyDataSetChanged()
                lineChart.data = LineData(dataSet)
                Timber.e("Num entries = ${dataSet.entryCount}")
                xHighlight?.let {
                    lineChart.highlightValue(it, 0)
                }
            }
            lineChart.invalidate()
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .testTag("UvChart")
    )
}

@Preview(showBackground = true)
@Composable
fun UvChartNoDataPreview() {
    MaterialTheme {
        UvChartWithState(UvChartState.NoData)
    }
}