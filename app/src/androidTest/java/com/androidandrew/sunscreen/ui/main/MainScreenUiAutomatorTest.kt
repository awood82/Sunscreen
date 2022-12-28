package com.androidandrew.sunscreen.ui.main

import android.graphics.Point
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.ui.SunscreenApp
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.compose.get

class MainScreenUiAutomatorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    lateinit var uiDevice: UiDevice
    lateinit var userRepo: UserRepositoryImpl

    @Before
    fun setup() {
        composeTestRule.setContent {
            userRepo = get()
            runBlocking {
                userRepo.setLocation(FakeData.zip)
            }

            SunscreenTheme {
                SunscreenApp()
            }
        }
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    // TODO: UI State is never updated w/ the Compose version of the test. I assert w/ the Repo instead of the UI, but it should be able to do both.
//    @LargeTest
    @Test
    fun startTracking_continues_whenAppIsInTheBackground() {
        runBlocking {
            val trackingInfo = userRepo.getUserTracking(FakeData.localDate.toString())
            assertNull(trackingInfo)
        }

        val vitaminDProgressBar = uiDevice.findObjects(By.res("progress"))[1]
        val progressStart = vitaminDProgressBar.text.progressTextToInt()
        uiDevice.findObject(UiSelector().text("Start Tracking")).click()
        runBlocking { delay(5000) }
        val progressMid = vitaminDProgressBar.text.progressTextToInt()

        uiDevice.pressHome()
        runBlocking { delay(3000) }

        // Return to the app under test
        uiDevice.pressRecentApps()
        runBlocking { delay(100) }
        val middleOfScreen = uiDevice.getClickPosition(50f, 50f)
        uiDevice.click(middleOfScreen.x, middleOfScreen.y)

        runBlocking { delay(3000) } // Give some time for UI to refresh
        val progressEnd = vitaminDProgressBar.text.progressTextToInt()

        runBlocking {
            val trackingInfo = userRepo.getUserTracking(FakeData.localDate.toString())
            assertNotEquals(0.0, trackingInfo?.vitaminDProgress)
            assertNotEquals(0.0, trackingInfo?.burnProgress)
        }

//        assertNotEquals(progressMid, progressEnd)
//        assertTrue("start=$progressStart, mid=$progressMid, end=$progressEnd", progressEnd - progressMid >= progressMid - progressStart)
    }


    private fun searchZip(zip: String = FakeData.zip) {
        uiDevice.findObject(By.res("locationText")).text = zip
        uiDevice.findObject(By.res("locationBarSearch")).click()
    }

    private fun String.progressTextToInt(): Int {
        val spaceIndex = this.indexOf(' ')
        val amount = this.substring(0, spaceIndex)
        return amount.toInt()
    }

    // Returns an x,y coordinate relative to a percentage of the screen's
    // dimensions.
    private fun UiDevice.getClickPosition(xPercent: Float, yPercent: Float): Point {
        val width = this.displayWidth
        val height = this.displayHeight

        val x = xPercent * width / 100
        val y = yPercent * height / 100

//        Timber.d("click $x, $y")
        return Point(x.toInt(), y.toInt())
    }
}