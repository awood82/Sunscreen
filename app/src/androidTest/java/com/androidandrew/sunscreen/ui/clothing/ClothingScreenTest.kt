package com.androidandrew.sunscreen.ui.clothing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performScrollTo
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.onNodeWithContentDescriptionId
import com.androidandrew.sunscreen.util.onNodeWithStringId
import org.junit.Rule
import org.junit.Test

class ClothingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ui_showsExpectedElements() {
        composeTestRule.setContent {
            ClothingScreen(onContinuePressed = {})
        }

        composeTestRule.apply {
            onNodeWithStringId(R.string.clothing_screen_title).assertIsDisplayed()
            onNodeWithStringId(R.string.clothing_screen_instructions).assertIsDisplayed()
            onNodeWithContentDescriptionId(R.string.clothing_top_nothing).assertIsDisplayed()
            onNodeWithContentDescriptionId(R.string.clothing_top_some).assertIsDisplayed()
            onNodeWithContentDescriptionId(R.string.clothing_top_covered).assertIsDisplayed()
            onNodeWithContentDescriptionId(R.string.clothing_bottom_nothing).assertIsDisplayed()
            onNodeWithContentDescriptionId(R.string.clothing_bottom_some).assertIsDisplayed()
            onNodeWithContentDescriptionId(R.string.clothing_bottom_covered).assertIsDisplayed()
        }
    }
}