package com.androidandrew.sunscreen.ui.location

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LocationBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchIcon_whenClicked_callsOnLocationSearched_withTextEntry() {
        var textEntry = "1"
        composeTestRule.setContent {
            LocationBar(
                onLocationSearched = { textEntry = it },
                initialText = "1"
            )
        }

        composeTestRule.onNodeWithText("1").performTextReplacement("123")
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        assertEquals("123", textEntry)
    }

    @Test
    fun keyboardSearchAction_whenClicked_callsOnLocationSearched_withTextEntry() {
        var textEntry = "1"
        composeTestRule.setContent {
            LocationBar(
                onLocationSearched = { textEntry = it },
                initialText = "1"
            )
        }

        composeTestRule.onNodeWithText("1").performTextReplacement("123")
        composeTestRule.onNodeWithText("123").performImeAction()
        assertEquals("123", textEntry)
    }
}