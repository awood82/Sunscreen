package com.androidandrew.sunscreen.ui.chart

import android.text.format.DateFormat
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import timber.log.Timber

private const val UNKNOWN = -1.0f

@Composable
fun UvChartWithState(
    uiState: UvChartState,
    onEvent: (UvChartEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is UvChartState.NoData -> {
            UvChart(
                modifier = modifier,
                onValueSelected = { onEvent(UvChartEvent.Touch(it.first.toInt(), it.second.toInt())) }
            )
        }
        is UvChartState.HasData -> {
            UvChart(
                modifier = modifier,
                dataSet = uiState.data,
                xHighlight = uiState.xHighlight,
                onValueSelected = { onEvent(UvChartEvent.Touch(it.first.toInt(), it.second.toInt())) }
            )
        }
    }
}

@Composable
fun UvChart(
    modifier: Modifier = Modifier,
    dataSet: LineDataSet? = null,
    xHighlight: Float? = null,
    chartFormatter: UvChartFormatter = UvChartFormatter(LocalContext.current),
    use24HourTime: Boolean = DateFormat.is24HourFormat(LocalContext.current),
    onValueSelected: (Pair<Float, Float>) -> Unit
) {
    Timber.e("lineDataSet null? ${dataSet == null}")
    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                chartFormatter.formatChart(
                    lineChart = this,
                    use24HourTime = use24HourTime
                )
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let {
                            onValueSelected(Pair(it.x, it.y))
                        } ?: onValueSelected(Pair(UNKNOWN, UNKNOWN))
                    }

                    override fun onNothingSelected() {}
                })
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

@Preview(showBackground = true)
@Composable
fun UvChartNoDataPreview() {
    MaterialTheme {
        UvChartWithState(UvChartState.NoData, {})
    }
}