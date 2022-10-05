package com.androidandrew.sunscreen.chart

import com.androidandrew.sunscreen.ui.chart.UvValueFormatter
import com.github.mikephil.charting.data.Entry
import org.junit.Assert.assertEquals
import org.junit.Test

class UvValueFormatterTest {

    private val uvValueFormatter = UvValueFormatter()

    @Test
    fun values_areAlways_displayedAsInt() {
        assertEquals("3", uvValueFormatter.getPointLabel(Entry(1.0F, 3.0F)))
        assertEquals("3", uvValueFormatter.getPointLabel(Entry(1.0F, 3.1F)))
        assertEquals("3", uvValueFormatter.getPointLabel(Entry(1.0F, 3.01F)))

    }
}