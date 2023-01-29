package com.androidandrew.sunscreen.ui.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.util.onNodeWithStringId
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.ui.SunscreenApp
import com.androidandrew.sunscreen.util.onNodeWithContentDescriptionId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.compose.get

class AppNavHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    private fun setupNavController(withOnboarded: Boolean) {
        // Init the navigation controller
        composeTestRule.setContent {
            val userSettingsRepo: UserSettingsRepository = get()
            runBlocking {
                userSettingsRepo.setIsOnboarded(withOnboarded)
            }

            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            SunscreenApp(useWideLayout = false, navController = navController)
        }
    }

    @Test
    fun startDestination_ifOnboardingIsComplete_isMainScreen() {
        setupNavController(withOnboarded = true)

        assertDestinationIs(AppDestination.Main.name)
    }

    @Test
    fun startDestination_ifOnboardingIsIncomplete_navigatesToLocationScreen() {
        setupNavController(withOnboarded = false)

        assertDestinationIs(AppDestination.Location.name)
    }

    @Test
    fun locationScreen_whenValidLocationIsSearched_navigatesToSkinTypeScreen() {
        navigateToLocationScreen()

        performLocationSearch(FakeData.zip)

        assertDestinationIs(AppDestination.SkinType.name)
    }

    @Test
    fun skinTypeScreen_whenSkinTypeIsSelected_navigatesToClothingScreen() {
        navigateToSkinTypeScreen()

        composeTestRule.onNodeWithStringId(R.string.type_1_title).performClick()

        assertDestinationIs(AppDestination.Clothing.name)
    }

    @Test
    fun clothingScreen_whenContinueIsPressed_navigatesToMainScreen() {
        navigateToClothingScreen()

        composeTestRule.onNodeWithStringId(R.string.clothing_screen_done).performClick()

        assertDestinationIs(AppDestination.Main.name)
    }

    @Test
    fun backButton_onMainScreen_afterOnboarding_doesNotReturnToOnboardingScreens() {
        navigateThroughOnboardingFlow()

        navigateBack()

        val navDestination = getCurrentNavDestination() ?: ""
        assertTrue(
            (navDestination != AppDestination.Location.name) or
            (composeTestRule.activityRule.scenario.state == Lifecycle.State.DESTROYED)
        )
    }

    @Test
    fun backButton_onSkinTypeScreen_returnsToLocationScreen() {
        navigateToSkinTypeScreen()

        navigateBack()

        assertDestinationIs(AppDestination.Location.name)
    }

    @Test
    fun backButton_onClothingScreen_returnsToSkinTypeScreen() {
        navigateToClothingScreen()

        navigateBack()

        assertDestinationIs(AppDestination.SkinType.name)
    }


    private fun performLocationSearch(zip: String) {
        composeTestRule.onNodeWithStringId(R.string.current_location).apply {
            performTextInput(zip)
            performImeAction()
        }
        // This waits for the keyboard to close. Otherwise the test fails and drives me crazy debugging.
        awaitIdle()
    }

    private fun navigateToLocationScreen() {
        setupNavController(withOnboarded = false)
    }

    private fun navigateToSkinTypeScreen() {
        navigateToLocationScreen()
        performLocationSearch(FakeData.zip)
    }

    private fun navigateToClothingScreen() {
        navigateToSkinTypeScreen()
        composeTestRule.onNodeWithStringId(R.string.type_1_title).performClick()
        awaitIdle()
    }

    private fun navigateThroughOnboardingFlow() {
        navigateToClothingScreen()
        composeTestRule.apply {
            onNodeWithContentDescriptionId(R.string.clothing_top_some).performClick()
            onNodeWithContentDescriptionId(R.string.clothing_bottom_some).performClick()
            onNodeWithStringId(R.string.clothing_screen_done).performClick()
        }

        awaitIdle()
    }

    private fun navigateBack() {
        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        awaitIdle()
    }

    private fun assertDestinationIs(name: String) {
        awaitIdle()
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

    // Wait for the keyboard to close, or for the navHost destination to update.
    private fun awaitIdle() {
        runBlocking {
            composeTestRule.awaitIdle()
        }
    }
}