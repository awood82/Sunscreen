package com.androidandrew.sunscreen.model.uv

import com.androidandrew.sunscreen.model.UvPrediction
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet

fun UvPrediction.toChartData(label: String = ""): LineDataSet {
    val entries = mutableListOf<Entry>()
    for (point in this) {
        entries.add(Entry(point.time.hour.toFloat(), point.uvIndex.toFloat()))
    }
    return LineDataSet(entries, label)
}