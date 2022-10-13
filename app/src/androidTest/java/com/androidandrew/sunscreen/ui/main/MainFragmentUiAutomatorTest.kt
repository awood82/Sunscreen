package com.androidandrew.sunscreen.ui.main

import android.widget.ProgressBar
import androidx.fragment.app.testing.withFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
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
    }

    @LargeTest
    @Test
    fun startTracking_continues_whenAppIsInTheBackground() {
        val vitaminDProgressBar: ProgressBar = fragmentScenario.withFragment { requireActivity().findViewById(R.id.progressVitaminD) }
        searchZip()

        val progressStart = vitaminDProgressBar.progress
        onView(withId(R.id.trackingButton)).perform(click())
        runBlocking { delay(5000) }
        val progressMid = vitaminDProgressBar.progress

        uiDevice.pressHome()
        runBlocking { delay(3000) }

        // Return to the app under test
        uiDevice.pressRecentApps()
        uiDevice.pressRecentApps()

        runBlocking { delay(2000) } // Give some time for UI to refresh
        val progressEnd = vitaminDProgressBar.progress

        assertNotEquals(progressMid, progressEnd)
        assertTrue("start=$progressStart, mid=$progressMid, end=$progressEnd", progressEnd - progressMid >= progressMid - progressStart)
    }
}