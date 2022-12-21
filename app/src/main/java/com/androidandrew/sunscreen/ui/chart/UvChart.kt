package com.androidandrew.sunscreen.ui.chart

import android.text.format.DateFormat
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun UvChartWithState(
    uiState: UvChartState,
    modifier: Modifier = Modifier
) {
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
    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                if (dataSet != null) {
                    chartFormatter.formatDataSet(dataSet)
                    chartFormatter.formatChart(
                        lineChart = this,
                        use24HourTime = use24HourTime
                    )
                    data = LineData(dataSet)
                    xHighlight?.let {
                        highlightValue(it, 0)
                    }
                    invalidate()
                }
            }
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