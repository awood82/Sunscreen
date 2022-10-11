package com.androidandrew.sunscreen.ui.chart

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet

class UvChartFormatter(context: Context) {

    private val plotLineColor = Color.parseColor("#FFAAAAAA")
    private val primaryTextColor = when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
        Configuration.UI_MODE_NIGHT_YES -> Color.parseColor("#FFFFFFFF")
        Configuration.UI_MODE_NIGHT_NO -> Color.parseColor("#FF000000")
        else -> plotLineColor
    }
    private val uvColors = listOf(
        Color.parseColor("#FF004400"),
        Color.parseColor("#FF00CC00"),
        Color.parseColor("#FF00CC00"),
        Color.parseColor("#FFCCCC00"),
        Color.parseColor("#FFCCCC00"),
        Color.parseColor("#FFCCCC00"),
        Color.parseColor("#FFFF8800"),
        Color.parseColor("#FFFF8800"),
        Color.parseColor("#FFFF0000"),
        Color.parseColor("#FFFF0000"),
        Color.parseColor("#FFFF0000"),
        Color.parseColor("#FFCC00FF"),
        Color.parseColor("#FFCC00FF"),
        Color.parseColor("#FFCC00FF"),
        Color.parseColor("#FFCC00FF")
    )

    fun formatDataSet(lineDataSet: LineDataSet) {
        setupDrawMode(lineDataSet)
        setupColors(lineDataSet)
    }

    fun formatChart(lineChart: LineChart, use24HourTime: Boolean) {
        setupAxes(lineChart, use24HourTime)
        lineChart.legend.isEnabled = false
        lineChart.description.isEnabled = false
    }

    private fun setupAxes(lineChart: LineChart, use24HourTime: Boolean) {
        lineChart.xAxis.apply {
            textColor = primaryTextColor
            valueFormatter = TimeAxisFormatter(use24HourTime)
            granularity = 0.1f
            isGranularityEnabled = true
        }
        lineChart.axisLeft.apply {
            textColor = primaryTextColor
            axisMinimum = 0.0f  // Y-axis starts at 0
            axisMaximum = 12.0f // Y-axis goes a little past extreme UV 11
            granularity = 1.0f
            isGranularityEnabled = true
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
        lineDataSet.color = plotLineColor
        lineDataSet.valueTextColor = primaryTextColor
        val maxUv = lineDataSet.yMax.toInt() + 1
        val upperColorIndex = Integer.min(maxUv, uvColors.size - 1)
        val dynamicGradient = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            uvColors.subList(0, upperColorIndex).toIntArray())
        lineDataSet.fillDrawable = dynamicGradient

        lineDataSet.isHighlightEnabled = true
        lineDataSet.valueFormatter = UvValueFormatter()
    }
}