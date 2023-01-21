package com.androidandrew.sunscreen.ui.clothing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollTo
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.onNodeWithStringId
import org.junit.Rule
import org.junit.Test

class ClothingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ui_showsExpectedElements() {
        composeTestRule.setContent {
            ClothingScreen(onClothingSelected = {})
        }

        composeTestRule.onNodeWithStringId(R.string.clothing_screen_title).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.clothing_screen_instructions).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.clothing_shorts_no_shirt).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.clothing_pants_no_shirt).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.clothing_shorts_t_shirt).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.clothing_pants_t_shirt).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.clothing_pants_long_sleeve_shirt).performScrollTo().assertIsDisplayed()
    }
}