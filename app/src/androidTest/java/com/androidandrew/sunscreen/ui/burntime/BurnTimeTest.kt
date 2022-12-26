package com.androidandrew.sunscreen.ui.burntime

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
import org.junit.Rule
import org.junit.Test

class BurnTimeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun burnTime_displaysStrings() {
        composeTestRule.setContent {
            SunscreenTheme {
                BurnTime("Burn time text")
            }
        }

        composeTestRule.apply {
            onNodeWithText("You could burn in").assertIsDisplayed()
            onNodeWithText("Burn time text").assertIsDisplayed()
        }
    }

    @Test
    fun uiState_whenUnknown_displaysUnknown() {
        composeTestRule.setContent {
            SunscreenTheme {
                BurnTimeWithState(BurnTimeUiState.Unknown)
            }
        }

        composeTestRule.apply {
            onNodeWithText("You could burn in").assertIsDisplayed()
            onNodeWithText("Unknown").assertIsDisplayed()
        }
    }

    @Test
    fun uiState_whenUnlikely_displaysUnlikely() {
        composeTestRule.setContent {
            SunscreenTheme {
                BurnTimeWithState(BurnTimeUiState.Unlikely)
            }
        }

        composeTestRule.apply {
            onNodeWithText("You could burn in").assertIsDisplayed()
            onNodeWithText("No burn expected").assertIsDisplayed()
        }
    }

    @Test
    fun uiState_whenKnown_displaysMinutes() {
        composeTestRule.setContent {
            SunscreenTheme {
                BurnTimeWithState(BurnTimeUiState.Known(15))
            }
        }

        composeTestRule.apply {
            onNodeWithText("You could burn in").assertIsDisplayed()
            onNodeWithText("15 minutes").assertIsDisplayed()
        }
    }
}