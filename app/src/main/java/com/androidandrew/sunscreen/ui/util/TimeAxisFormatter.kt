package com.androidandrew.sunscreen.ui.util

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimeAxisFormatter(private val use24HourTime: Boolean) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return LocalTime.of(value.toInt(), 0).format(getLocalTimeFormatter())
    }

    private fun getLocalTimeFormatter(): DateTimeFormatter {
        return when (use24HourTime) {
            true -> DateTimeFormatter.ofPattern("H:mm")
            false -> DateTimeFormatter.ofPattern("h:mm a")
        }
    }
}