package com.androidandrew.sunscreen.ui.chart

import com.github.mikephil.charting.data.LineDataSet

sealed interface UvChartState {
    data class HasData(val data: LineDataSet, val highlight: Float): UvChartState
    object NoData: UvChartState
}