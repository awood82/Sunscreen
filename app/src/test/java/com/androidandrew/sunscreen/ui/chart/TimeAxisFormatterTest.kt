package com.androidandrew.sunscreen.ui.chart

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeAxisFormatterTest {

    @Test
    fun getAxisLabel_whenUsing24HourFormat_displaysCorrectly() {
        val formatter = TimeAxisFormatter(use24HourTime = true)

        assertEquals("8:00", formatter.getAxisLabel(8.0F, null))
        assertEquals("12:00", formatter.getAxisLabel(12.0F, null))
        assertEquals("16:00", formatter.getAxisLabel(16.0F, null))
    }

    @Test
    fun getAxisLabel_whenUsing12HourFormat_displaysCorrectly() {
        val formatter = TimeAxisFormatter(use24HourTime = false)

        assertEquals("8AM", formatter.getAxisLabel(8.0F, null))
        assertEquals("12PM", formatter.getAxisLabel(12.0F, null))
        assertEquals("4PM", formatter.getAxisLabel(16.0F, null))
    }
}