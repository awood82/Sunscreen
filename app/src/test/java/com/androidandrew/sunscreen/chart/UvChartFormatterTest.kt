package com.androidandrew.sunscreen.chart

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.ui.chart.TimeAxisFormatter
import com.androidandrew.sunscreen.ui.chart.UvChartFormatter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UvChartFormatterTest {

    private val fakeEntries = listOf<Entry>(
        Entry(8f, 3.0f),
        Entry(10f, 5.0f) )

    private val context = mockk<Context>(relaxed = true)
    private val lineChart = LineChart(context)
    private val lineDataSet = LineDataSet(fakeEntries, "label")
    private val chartFormatter = UvChartFormatter(context)

    @Test
    fun formatChart_xAxis_displaysTime() {
        chartFormatter.formatChart(lineChart, use24HourTime = false)

        assertTrue(lineChart.xAxis.valueFormatter is TimeAxisFormatter)
    }

    @Test
    fun formatChart_yAxisOnRight_isHidden() {
        chartFormatter.formatChart(lineChart, use24HourTime = false)

        assertFalse(lineChart.axisRight.isEnabled)
    }

    @Test
    fun formatChart_yAxisOnLeft_goesFrom0To12By1() {
        chartFormatter.formatChart(lineChart, use24HourTime = false)

        assertEquals(0.0f, lineChart.axisLeft.axisMinimum)
        assertEquals(12.0f, lineChart.axisLeft.axisMaximum)
        assertEquals(1.0f, lineChart.axisLeft.granularity)
    }

    @Test
    fun formatDataSet_drawsBezierCurve() {
        chartFormatter.formatDataSet(lineDataSet)

        assertEquals(LineDataSet.Mode.CUBIC_BEZIER, lineDataSet.mode)
    }
}