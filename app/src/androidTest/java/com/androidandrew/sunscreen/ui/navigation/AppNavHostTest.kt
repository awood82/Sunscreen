package com.androidandrew.sunscreen.ui.navigation

import android.os.Bundle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.ui.SunscreenApp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.compose.get

class AppNavHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    private fun setupNavController(withLocation: String) {
        // Init the navigation controller
        composeTestRule.setContent {
            val userSettingsRepo: UserSettingsRepository = get()
            runBlocking {
                userSettingsRepo.setLocation(withLocation)
            }

            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            SunscreenApp(navController = navController)
        }
    }

    @Test
    fun startDestination_ifOnboardingIsComplete_isMainScreen() {
        setupNavController(withLocation = FakeData.zip)

        assertDestinationIs(AppDestination.Main.name)
    }

    @Test
    fun startDestination_ifOnboardingIsIncomplete_navigatesToLocationScreen() {
        setupNavController(withLocation = "")

        assertDestinationIs(AppDestination.Location.name)
    }


    private fun assertDestinationIs(name: String) {
        assertEquals(name, getCurrentNavDestination())
    }

    private fun getCurrentNavDestination(): String? {
        return navController.currentBackStackEntry?.destination?.route?.substringBefore('/')
    }

    private fun assertNavArgIs(key: String, expectedValue: String) {
        assertEquals(expectedValue, getCurrentArguments()?.getString(key))
    }

    private fun getCurrentArguments(): Bundle? {
        return navController.currentBackStackEntry?.arguments
    }

//    private fun getUpButton(): SemanticsNodeInteraction {
//        return composeTestRule.onNodeWithContentDescription(
//            composeTestRule.activity.getString(R.string.navigate_back))
//    }
}