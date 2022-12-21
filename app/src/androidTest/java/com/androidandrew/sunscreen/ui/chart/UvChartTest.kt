package com.androidandrew.sunscreen.ui.chart

import androidx.activity.ComponentActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.androidandrew.sharedtest.model.FakeUvPredictions
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UvChartTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var fakeData: LineDataSet

    @Before
    fun setup() {
        val predictions = FakeUvPredictions.forecast
        val entries = mutableListOf<Entry>()
        for (point in predictions) {
            entries.add(Entry(point.time.hour.toFloat(), point.uvIndex.toFloat()))
        }
        fakeData = LineDataSet(entries, "")
    }

    @Test
    fun uvChart_withNoData_showsNoChartDataAvailable() {
        composeTestRule.setContent {
            MaterialTheme {
                UvChartWithState(uiState = UvChartState.NoData)
            }
        }

        composeTestRule.onNodeWithTag("UvChart").assertIsDisplayed()
    }

    @Test
    fun uvChart_withDataSet_showsData() {
        composeTestRule.setContent {
            MaterialTheme {
                UvChart(
                    dataSet = fakeData,
                    xHighlight = 10.0f
                )
            }
        }

        composeTestRule.onNodeWithTag("UvChart").assertIsDisplayed()
        // TODO: Screenshot test would show data
    }
}