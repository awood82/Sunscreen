package com.androidandrew.sunscreen.ui.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ProgressTrackerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun labels_areDisplayed_andNotDependent() {
        composeTestRule.setContent {
            ProgressTracker(
                progress = 20f,
                label = "the label",
                progressText = "80%"
            )
        }

        composeTestRule.onNodeWithText("the label").assertIsDisplayed()
        composeTestRule.onNodeWithText("80%").assertIsDisplayed()
    }
}