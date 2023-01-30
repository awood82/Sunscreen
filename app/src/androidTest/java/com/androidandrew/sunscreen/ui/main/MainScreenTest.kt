package com.androidandrew.sunscreen.ui.main

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.ui.SunscreenApp
import com.androidandrew.sunscreen.util.onNodeWithStringId
import com.androidandrew.sunscreen.util.setOrientation
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.onNodeWithContentDescriptionId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject
import java.io.IOException

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeService = FakeEpaService
    private val fakeUserSettingsRepo: UserSettingsRepository by inject(UserSettingsRepository::class.java)
    private val serviceDelayPeriodMs = 200L

    @After
    fun cleanup() {
        fakeService.exception = null
        fakeService.delayMs = 0
    }

    // TODO: Why isn't the repository reset, even if FakeDatabaseWrapper.clearDatabase() is called?
    @Test
    fun init_ifNetworkError_showsErrorSnackbar() {
        runBlocking {
            fakeUserSettingsRepo.setIsOnboarded(true)
            fakeUserSettingsRepo.setLocation("78910") // Pick unused value since database might not be reset
        }
        val errorMessage = "An error has occurred"
        fakeService.exception = IOException(errorMessage)

        composeTestRule.setContent {
            SunscreenApp(useWideLayout = false)
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun init_withWideLayout_showsAllInfo() {
        runBlocking {
            fakeUserSettingsRepo.setIsOnboarded(true)
            fakeUserSettingsRepo.setLocation("78911") // Pick unused value since database might not be reset
        }

        try {
            composeTestRule.setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

            composeTestRule.setContent {
                SunscreenApp(useWideLayout = true)
            }

            composeTestRule.apply {
                onNodeWithStringId(R.string.current_location).assertIsDisplayed()
                onNodeWithStringId(R.string.could_burn).assertIsDisplayed()
                onNodeWithTag("UvChart").assertIsDisplayed()
                onNodeWithStringId(R.string.sunburn).assertIsDisplayed()
                onNodeWithStringId(R.string.vitamin_d).assertIsDisplayed()
                onNodeWithStringId(R.string.start_tracking).assertIsDisplayed()
            }
            runBlocking { delay(1000)}
        } finally {
            composeTestRule.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    @Test
    fun search_showsLoadingIndicator() {
        runBlocking {
            fakeUserSettingsRepo.setIsOnboarded(true)
        }

        composeTestRule.setContent {
            SunscreenApp(useWideLayout = false)
        }

        fakeService.delayMs = serviceDelayPeriodMs
        composeTestRule.apply {
            onNodeWithStringId(R.string.current_location).performTextReplacement("78912")
            onNodeWithContentDescriptionId(R.string.search).performClick()
        }

        composeTestRule.apply {
            onNodeWithTag("Loading").assertIsDisplayed()
        }
        awaitIdle()
        runBlocking { delay(serviceDelayPeriodMs * 2) }
        composeTestRule.apply {
            onNodeWithTag("Loading").assertDoesNotExist()
        }
    }

    @Test
    fun search_multipleTimes_hidesLoadingIndicatorWhenDone() {
        runBlocking {
            fakeUserSettingsRepo.setIsOnboarded(true)
        }

        composeTestRule.setContent {
            SunscreenApp(useWideLayout = false)
        }

        assertSearchShowsAndHidesLoadingIndicator(repeatTimes = 2)
    }

    private fun assertSearchShowsAndHidesLoadingIndicator(repeatTimes: Int) {
        fakeService.delayMs = serviceDelayPeriodMs
        for (zip in 91000 until 91000 + repeatTimes) {
            composeTestRule.apply {
                onNodeWithStringId(R.string.current_location).performTextReplacement(zip.toString())
                onNodeWithContentDescriptionId(R.string.search).performClick()
            }

            composeTestRule.onNodeWithTag("Loading").assertIsDisplayed()
            awaitIdle()
            runBlocking { delay(serviceDelayPeriodMs * 2) }
            composeTestRule.apply {
                onNodeWithTag("Loading").assertDoesNotExist()
            }
        }
    }

    // Wait for the keyboard to close, or for the navHost destination to update.
    private fun awaitIdle() {
        runBlocking {
            composeTestRule.awaitIdle()
        }
    }
}