package com.androidandrew.sunscreen.ui.location

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
        var search = ""
        composeTestRule.setContent {
            LocationBar(
                value = "123",
                onValueChange = {},
                onLocationSearched = { search = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").performClick()

        assertEquals("123", search)
    }

    @Test
    fun keyboardSearchAction_whenClicked_callsOnLocationSearched_withTextEntry() {
        var search = ""
        composeTestRule.setContent {
            LocationBar(
                value = "123",
                onValueChange = {},
                onLocationSearched = { search = it }
            )
        }

        composeTestRule.onNodeWithText("123").performImeAction()

        assertEquals("123", search)
    }

    @Test
    fun withState_callsEvents() {
        val textEntry = mutableStateOf("1")
        val search = mutableStateOf("")

        composeTestRule.setContent {
            var textValue by rememberSaveable { mutableStateOf("1") }

            LocationBarWithState(
                uiState = LocationBarState(textValue),
                onEvent = {
                    when (it) {
                        is LocationBarEvent.TextChanged -> {
                            textValue = it.text
                            textEntry.value = it.text // Expose this for test assertion
                        }
                        is LocationBarEvent.LocationSearched -> { search.value = it.location }
                    }
                }
            )
        }

        composeTestRule.apply {
            onNodeWithText("1").performTextReplacement("123")
            onNodeWithText("123").performImeAction()
        }

        assertEquals("123", search.value)
        assertEquals("123", textEntry.value)
    }
}