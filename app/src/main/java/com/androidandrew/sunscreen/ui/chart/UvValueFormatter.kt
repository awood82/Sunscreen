package com.androidandrew.sunscreen.ui.chart

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter

class UvValueFormatter : ValueFormatter() {
    override fun getPointLabel(entry: Entry?): String {
        return entry?.y?.toInt().toString()
    }
}