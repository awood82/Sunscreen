package com.androidandrew.sunscreen.ui.tracking

import androidx.activity.ComponentActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.onNodeWithStringId
import org.junit.Rule
import org.junit.Test

class UvTrackingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    val dummyState = UvTrackingState(
        buttonLabel = R.string.start_tracking,
        buttonEnabled = false,
//        spf = "1",
//        isOnSnowOrWater = true,
//        sunburnProgressLabelMinusUnits = 10,
//        sunburnProgress0to1 = 0.1f,
//        vitaminDProgressLabelMinusUnits = 200,
//        vitaminDProgress0to1 = 0.05f
    )

    @Test
    fun uvTrackingWithState_withSomeDefaultValues_displaysStrings() {
        composeTestRule.setContent {
            MaterialTheme {
                UvTrackingWithState(
                    uiState = dummyState,
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithStringId(R.string.start_tracking).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.start_tracking).assertIsNotEnabled()
//        composeTestRule.onNodeWithText("1").assertIsDisplayed()
//        composeTestRule.onNodeWithStringId(R.string.on_snow_or_water).assertIsDisplayed()
//        composeTestRule.onNodeWithTag("checkOnSnowOrWater").assertIsOn()
//        composeTestRule.onNodeWithStringId(R.string.sunburn).assertIsDisplayed()
//        composeTestRule.onNodeWithStringId(R.string.sunburn_progress, "10").assertIsDisplayed()
//        composeTestRule.onNodeWithStringId(R.string.vitamin_d).assertIsDisplayed()
//        composeTestRule.onNodeWithStringId(R.string.vitamin_d_progress, "200").assertIsDisplayed()
    }

}