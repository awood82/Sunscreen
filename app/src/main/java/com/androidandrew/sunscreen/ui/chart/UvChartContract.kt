package com.androidandrew.sunscreen.ui.chart

import com.github.mikephil.charting.data.LineDataSet

sealed interface UvChartState {
    data class HasData(val data: LineDataSet, val xHighlight: Float) : UvChartState
    object NoData : UvChartState
}

sealed interface UvChartEvent {
    data class Touch(val xPos: Int, val yPos: Int) : UvChartEvent
}