package com.androidandrew.sunscreen.ui.chart

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.androidandrew.sharedtest.model.FakeUvPredictions
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
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
    fun uvChartWithState_withNoData_showsNoChartDataAvailable() {
        composeTestRule.setContent {
            SunscreenTheme {
                UvChartWithState(
                    uiState = UvChartState.NoData,
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("UvChart").assertIsDisplayed()
    }

    @Test
    fun uvChartWithState_withDataSet_showsData() {
        composeTestRule.setContent {
            SunscreenTheme {
                UvChartWithState(
                    uiState = UvChartState.HasData(
                        data = fakeData,
                        xHighlight = 10.0f
                    ),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("UvChart").assertIsDisplayed()
    }
}