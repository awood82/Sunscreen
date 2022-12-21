package com.androidandrew.sunscreen.ui.burntime

import androidx.activity.ComponentActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.onNodeWithStringId
import org.junit.Rule
import org.junit.Test

class BurnTimeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun burnTime_displaysStrings() {
        composeTestRule.setContent {
            MaterialTheme {
                BurnTime("Burn time text")
            }
        }

        composeTestRule.onNodeWithStringId(R.string.could_burn).assertIsDisplayed()
        composeTestRule.onNodeWithText("Burn time text").assertIsDisplayed()
    }

    @Test
    fun uiState_whenUnknown_displaysUnknown() {
        composeTestRule.setContent {
            MaterialTheme {
                BurnTimeWithState(BurnTimeState.Unknown)
            }
        }

        composeTestRule.onNodeWithStringId(R.string.could_burn).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.unknown).assertIsDisplayed()
    }

    @Test
    fun uiState_whenUnlikely_displaysUnlikely() {
        composeTestRule.setContent {
            MaterialTheme {
                BurnTimeWithState(BurnTimeState.Unlikely)
            }
        }

        composeTestRule.onNodeWithStringId(R.string.could_burn).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.unlikely).assertIsDisplayed()
    }

    @Test
    fun uiState_whenKnown_displaysMinutes() {
        composeTestRule.setContent {
            MaterialTheme {
                BurnTimeWithState(BurnTimeState.Known(15))
            }
        }

        composeTestRule.onNodeWithStringId(R.string.could_burn).assertIsDisplayed()
        composeTestRule.onNodeWithText("15 minutes").assertIsDisplayed()
    }
}