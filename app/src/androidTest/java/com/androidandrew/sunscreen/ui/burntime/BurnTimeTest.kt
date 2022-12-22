package com.androidandrew.sunscreen.ui.burntime

import androidx.activity.ComponentActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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

        composeTestRule.onNodeWithText("You could burn in").assertIsDisplayed()
        composeTestRule.onNodeWithText("Burn time text").assertIsDisplayed()
    }

    @Test
    fun uiState_whenUnknown_displaysUnknown() {
        composeTestRule.setContent {
            MaterialTheme {
                BurnTimeWithState(BurnTimeUiState.Unknown)
            }
        }

        composeTestRule.onNodeWithText("You could burn in").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed()
    }

    @Test
    fun uiState_whenUnlikely_displaysUnlikely() {
        composeTestRule.setContent {
            MaterialTheme {
                BurnTimeWithState(BurnTimeUiState.Unlikely)
            }
        }

        composeTestRule.onNodeWithText("You could burn in").assertIsDisplayed()
        composeTestRule.onNodeWithText("No burn expected").assertIsDisplayed()
    }

    @Test
    fun uiState_whenKnown_displaysMinutes() {
        composeTestRule.setContent {
            MaterialTheme {
                BurnTimeWithState(BurnTimeUiState.Known(15))
            }
        }

        composeTestRule.onNodeWithText("You could burn in").assertIsDisplayed()
        composeTestRule.onNodeWithText("15 minutes").assertIsDisplayed()
    }
}