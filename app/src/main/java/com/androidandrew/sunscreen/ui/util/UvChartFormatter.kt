package com.androidandrew.sunscreen.ui.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet

class UvChartFormatter(private val context: Context) {

    private val colors = listOf(
        Color.parseColor("#FF004400"),
        Color.parseColor("#FF00FF00"),
        Color.parseColor("#FF00FF00"),
        Color.parseColor("#FFFFFF00"),
        Color.parseColor("#FFFFFF00"),
        Color.parseColor("#FFFFFF00"),
        Color.parseColor("#FFFF8800"),
        Color.parseColor("#FFFF8800"),
        Color.parseColor("#FFFF0000"),
        Color.parseColor("#FFFF0000"),
        Color.parseColor("#FFFF0000"),
        Color.parseColor("#FFFF00FF"),
        Color.parseColor("#FFFF00FF"),
        Color.parseColor("#FFFF00FF"),
        Color.parseColor("#FFFF00FF")
    )

    fun formatChart(lineChart: LineChart, use24HourTime: Boolean) {
        setupAxes(lineChart, use24HourTime)
    }

    fun formatDataSet(lineDataSet: LineDataSet) {
        setupDrawMode(lineDataSet)
        setupColors(lineDataSet)
    }

    private fun setupAxes(lineChart: LineChart, use24HourTime: Boolean) {
        lineChart.xAxis.apply {
            valueFormatter = TimeAxisFormatter(use24HourTime)
        }
        lineChart.axisLeft.apply {
            axisMinimum = 0.0f  // Y-axis starts at 0
            axisMaximum = 12.0f // Y-axis goes a little past extreme UV 11
            granularity = 1.0f
            isGranularityEnabled = true
//            textColor = context.resolveColorAttr
        }
        lineChart.axisRight.apply {
            isEnabled = false
        }
    }

    private fun setupDrawMode(lineDataSet: LineDataSet) {
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.setDrawFilled(true)
    }

    private fun setupColors(lineDataSet: LineDataSet) {
        lineDataSet.color = Color.parseColor("#FFAAAAAA") // Plot line
        val maxUv = lineDataSet.yMax.toInt() + 1
        val upperColorIndex = Integer.min(maxUv, colors.size - 1)
        val dynamicGradient = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            colors.subList(0, upperColorIndex).toIntArray())
        lineDataSet.fillDrawable = dynamicGradient
    }
}