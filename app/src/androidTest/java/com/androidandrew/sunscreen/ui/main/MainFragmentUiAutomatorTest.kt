package com.androidandrew.sunscreen.ui.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.FlakyTest
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.BaseUiAutomatorTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainFragmentUiAutomatorTest : BaseUiAutomatorTest() {

    private fun searchZip(zip: String = FakeData.zip) {
        onView(withId(R.id.editLocation)).perform(ViewActions.replaceText(zip))
        onView(withId(R.id.search)).perform(click())
        runBlocking { delay(5000) }
    }

    @FlakyTest
    @LargeTest
    @Test
    fun startTracking_continues_whenAppIsInTheBackground() {
        searchZip("92123")
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
        val middleOfScreen = getClickPosition(50f, 50f)
        uiDevice.click(middleOfScreen.x, middleOfScreen.y)

        runBlocking { delay(3000) } // Give some time for UI to refresh
        val progressEnd = vitaminDProgressBar.text.progressTextToInt()

        assertNotEquals(progressMid, progressEnd)
        assertTrue("start=$progressStart, mid=$progressMid, end=$progressEnd", progressEnd - progressMid >= progressMid - progressStart)
    }

    private fun String.progressTextToInt(): Int {
        val spaceIndex = this.indexOf(' ')
        val amount = this.substring(0, spaceIndex)
        return amount.toInt()
    }
}