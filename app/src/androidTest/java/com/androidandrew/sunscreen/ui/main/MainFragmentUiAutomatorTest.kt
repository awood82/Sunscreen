package com.androidandrew.sunscreen.ui.main

import android.graphics.Point
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.filters.LargeTest
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.compose.get

class MainFragmentUiAutomatorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    lateinit var uiDevice: UiDevice

    @Before
    fun setup() {
        composeTestRule.setContent {
            val userRepo: UserRepositoryImpl = get()
            runBlocking {
                userRepo.setLocation(FakeData.zip)
            }

            SunscreenTheme {
                SunscreenApp()
            }
        }
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
    
    @LargeTest
    @Test
    fun startTracking_continues_whenAppIsInTheBackground() {
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

        assertNotEquals(progressMid, progressEnd)
        assertTrue("start=$progressStart, mid=$progressMid, end=$progressEnd", progressEnd - progressMid >= progressMid - progressStart)
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