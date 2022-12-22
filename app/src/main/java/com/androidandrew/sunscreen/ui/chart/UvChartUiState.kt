package com.androidandrew.sunscreen.ui.chart

import com.github.mikephil.charting.data.LineDataSet

sealed interface UvChartUiState {
    data class HasData(val data: LineDataSet, val xHighlight: Float): UvChartUiState
    object NoData: UvChartUiState
}