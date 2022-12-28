package com.androidandrew.sunscreen.ui.tracking

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
import com.androidandrew.sunscreen.util.onNodeWithStringId
import org.junit.Rule
import org.junit.Test

class UvTrackingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val dummyState = UvTrackingState(
        isTracking = false,
        isTrackingPossible = false,
        spfOfSunscreenAppliedToSkin = "15",
        isOnSnowOrWater = true,
        sunburnProgressAmount = 10,
        sunburnProgressPercent0to1 = 0.1f,
        vitaminDProgressAmount = 200,
        vitaminDProgressPercent0to1 = 0.05f
    )

    @Test
    fun uvTrackingWithState_withSomeDefaultValues_displaysStrings() {
        composeTestRule.setContent {
            SunscreenTheme {
                UvTrackingWithState(
                    uiState = dummyState,
                    onEvent = {}
                )
            }
        }

        composeTestRule.apply {
            onNodeWithStringId(R.string.start_tracking).assertIsDisplayed()
            onNodeWithStringId(R.string.start_tracking).assertIsNotEnabled()
            onNodeWithText("15").assertIsDisplayed()
            onNodeWithStringId(R.string.on_snow_or_water).assertIsDisplayed()
            onNodeWithTag("checkOnSnowOrWater").assertIsOn()
            onNodeWithStringId(R.string.sunburn).assertIsDisplayed()
            onNodeWithStringId(R.string.sunburn_progress, 10).assertIsDisplayed()
            onNodeWithStringId(R.string.vitamin_d).assertIsDisplayed()
            onNodeWithStringId(R.string.vitamin_d_progress, 200).assertIsDisplayed()
        }
    }
}