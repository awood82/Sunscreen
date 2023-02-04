package com.androidandrew.sunscreen.ui.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
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
import com.androidandrew.sunscreen.util.waitUntilExists
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
        startWithLocationScreenInOnboardingFlow()

        performLocationSearch(FakeData.zip)

        assertDestinationIs(AppDestination.SkinType.name)
    }

    @Test
    fun skinTypeScreen_whenSkinTypeIsSelected_navigatesToClothingScreen() {
        navigateToSkinTypeScreen()

        selectSkinType()

        assertDestinationIs(AppDestination.Clothing.name)
    }

    @Test
    fun clothingScreen_whenContinueIsPressed_navigatesToMainScreen() {
        navigateToClothingScreen()

        selectClothing()

        assertDestinationIs(AppDestination.Main.name)
    }

    @Test
    fun mainScreen_ifSkinTypeButtonIsClicked_navigatesToSkinScreen_andSelectingReturnsToMain() {
        setupNavController(withOnboarded = true)

        clickSkinTypeSetting()

        assertDestinationIs(AppDestination.SkinType.name)

        selectSkinType()

        assertDestinationIs(AppDestination.Main.name)
    }

    @Test
    fun mainScreen_ifClothingButtonIsClicked_navigatesToSkinScreen_andSelectingReturnsToMain() {
        setupNavController(withOnboarded = true)

        clickClothingSetting()

        assertDestinationIs(AppDestination.Clothing.name)

        selectClothing()

        assertDestinationIs(AppDestination.Main.name)
    }

    @Test
    fun mainScreen_ifSkinTypeIsClicked_whenTrackingIsOn_afterReturningToMainScreen_trackingButtonSaysTracking() {
        setupNavController(withOnboarded = true)
        performLocationSearch(FakeData.zip)
        clickTrackingButton()

        assertThatStopTrackingIsVisible()
        clickSkinTypeSetting()
        selectSkinType()

        waitUntilMainScreenIsVisibleAfterLocationSearch()
        assertThatStopTrackingIsVisible()
    }

    @Test
    fun mainScreen_ifClothingButtonIsClicked_whenTrackingIsOn_afterReturningToMainScreen_trackingButtonSaysTracking() {
        setupNavController(withOnboarded = true)
        performLocationSearch(FakeData.zip)
        clickTrackingButton()

        assertThatStopTrackingIsVisible()
        clickClothingSetting()
        selectClothing()

        waitUntilMainScreenIsVisibleAfterLocationSearch()
        assertThatStopTrackingIsVisible()
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


    private fun startWithLocationScreenInOnboardingFlow() {
        setupNavController(withOnboarded = false)
    }

    private fun performLocationSearch(zip: String) {
        composeTestRule.onNodeWithStringId(R.string.current_location).apply {
            performTextInput(zip)
            performImeAction()
        }
        // This waits for the keyboard to close. Otherwise the test fails and drives me crazy debugging.
        awaitIdle()
    }

    private fun navigateToSkinTypeScreen() {
        startWithLocationScreenInOnboardingFlow()
        performLocationSearch(FakeData.zip)
    }

    private fun selectSkinType() {
        composeTestRule.onNodeWithStringId(R.string.type_1_title).performClick()
        awaitIdle()
    }

    private fun navigateToClothingScreen() {
        navigateToSkinTypeScreen()
        selectSkinType()
    }

    private fun selectClothing() {
        composeTestRule.onNodeWithStringId(R.string.clothing_screen_done).performClick()
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

    private fun clickSkinTypeSetting() {
        composeTestRule.onNodeWithContentDescriptionId(R.string.skin_type_title).performClick()
        composeTestRule.waitUntilExists(hasText(getString(R.string.type_1_title)))
    }

    private fun clickClothingSetting() {
        composeTestRule.onNodeWithContentDescriptionId(R.string.clothing_screen_title).performClick()
        composeTestRule.waitUntilExists(hasText(getString(R.string.clothing_screen_title)))
    }

    private fun clickTrackingButton() {
        composeTestRule.onNodeWithStringId(R.string.start_tracking).performClick()
        composeTestRule.waitUntilExists(hasText(getString(R.string.stop_tracking)))
    }

    private fun navigateBack() {
        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        awaitIdle()
    }

    private fun waitUntilMainScreenIsVisibleAfterLocationSearch() {
        composeTestRule.waitUntilExists(hasText(text = "Tracking", substring = true).and(isEnabled()))
    }

    private fun assertThatStopTrackingIsVisible() {
        composeTestRule.onNodeWithStringId(R.string.stop_tracking).assertIsDisplayed()
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

    private fun getString(@StringRes id: Int): String {
        return composeTestRule.activity.getString(id)
    }
}