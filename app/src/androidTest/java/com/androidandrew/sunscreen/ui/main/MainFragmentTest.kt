package com.androidandrew.sunscreen.ui.main

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.withFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.BaseUiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class MainFragmentTest: BaseUiTest() {

    private lateinit var fragmentScenario: FragmentScenario<MainFragment>

    @Before
    override fun setup() {
        // NOTE on fixing pinned emulator: adb shell am task lock stop
        super.setup()
        fragmentScenario = launchFragmentUnderTest()
    }

    @After
    override fun tearDown() {
        super.tearDown()
        try {
            fragmentScenario.withFragment { requireActivity().stopLockTask() }
        } catch (e: Exception) {
        }
    }

    @Test
    fun init_ifUserAndUvForecastExist_enablesStartTracking() {
        onView(withId(R.id.startButton)).check(matches(isEnabled()))
        onView(withId(R.id.stopButton)).check(matches(isNotEnabled()))
    }

    @Test
    fun whenTrackingStarted_stopIsEnabled() {
        onView(withId(R.id.startButton)).perform(click())

        onView(withId(R.id.startButton)).check(matches(isNotEnabled()))
        onView(withId(R.id.stopButton)).check(matches(isEnabled()))
    }

    @Test
    fun init_sunburnPercent_isDisplayed() {
        onView(withId(R.id.textSunburnProgress)).check(matches(withText("0%")))
    }

    @Test
    fun init_vitaminD_IU_isDisplayed() {
        onView(withId(R.id.textVitaminDProgress)).check(matches(withText("0 IU")))
    }
}