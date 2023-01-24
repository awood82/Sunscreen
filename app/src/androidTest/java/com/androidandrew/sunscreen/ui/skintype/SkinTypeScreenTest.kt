package com.androidandrew.sunscreen.ui.skintype

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollTo
import com.androidandrew.sunscreen.util.onNodeWithStringId
import com.androidandrew.sunscreen.R
import org.junit.Rule
import org.junit.Test

class SkinTypeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ui_showsExpectedElements() {
        composeTestRule.setContent {
            SkinTypeScreen(onSkinTypeSelected = {})
        }

        composeTestRule.onNodeWithStringId(R.string.skin_type_instructions).assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.type_1_title).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.type_2_title).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.type_3_title).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.type_4_title).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.type_5_title).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithStringId(R.string.type_6_title).performScrollTo().assertIsDisplayed()
    }
}