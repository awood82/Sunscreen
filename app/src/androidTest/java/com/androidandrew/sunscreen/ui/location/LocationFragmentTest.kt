package com.androidandrew.sunscreen.ui.location

import androidx.annotation.IntegerRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.MainActivity
import com.androidandrew.sunscreen.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class LocationFragmentTest {

    lateinit var navController: NavController

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun startInLocationFragment() {
        composeTestRule.activityRule.scenario.onActivity { mainActivity ->
            navController = findNavController(mainActivity, R.id.myNavHostFragment).also {
                it.navigate(R.id.locationFragment)
            }
        }
    }

    @Test
    fun searchButtonClick_whenZipIsInvalid_doesNotNavigate() {
        searchZip(zip = "123")

        assertNavigationScreenIs(R.id.locationFragment)
    }

    @Test
    fun searchButtonClick_whenZipIsValid_navigatesToMainScreen() {
        searchZip(FakeData.zip)

        assertNavigationScreenIs(R.id.mainFragment)
    }

    // TODO
    @Ignore("I'm not sure how to test this now that the init screen is involved")
    @Test
    fun navigatingBack_toLocationFragment_isDisabled() {
        searchZip(FakeData.zip)

        assertNavigationScreenIs(R.id.mainFragment)

        assertFalse(navController.navigateUp())
    }

    private fun searchZip(zip: String) {
        composeTestRule.onNodeWithText("").performTextReplacement(zip)
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        runBlocking { delay(500) }   // TODO: Should use Idling Resource instead
    }

    private fun assertNavigationScreenIs(@IntegerRes screen: Int) {
        assertEquals(screen, navController.currentBackStackEntry?.destination?.id)
    }
}