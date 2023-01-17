package com.androidandrew.sunscreen.ui.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.ui.SunscreenApp
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject
import java.io.IOException

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeService = FakeEpaService
    private val fakeUserSettingsRepo: UserSettingsRepository by inject(UserSettingsRepository::class.java)

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
            SunscreenApp()
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}