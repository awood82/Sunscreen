package com.androidandrew.sunscreen.ui.skintype

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class FitzpatrickSkinTypeRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ui_showsExpectedElements() {
        composeTestRule.setContent {
            FitzpatrickSkinTypeRow(
                title = "the title",
                description = "the description",
                example = "the example",
                color = Color.Blue)
        }

        composeTestRule.apply {
            onNodeWithText("the title").assertIsDisplayed()
            onNodeWithText("the description").assertIsDisplayed()
            onNodeWithText("the example").assertIsDisplayed()
        }
    }
}