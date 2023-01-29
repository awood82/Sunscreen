package com.androidandrew.sunscreen.ui.main

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.ui.SunscreenApp
import com.androidandrew.sunscreen.util.onNodeWithStringId
import com.androidandrew.sunscreen.util.setOrientation
import com.androidandrew.sunscreen.R
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
        } finally {
            composeTestRule.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }
}