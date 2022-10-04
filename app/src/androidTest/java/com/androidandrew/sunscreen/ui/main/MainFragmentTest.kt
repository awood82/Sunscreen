package com.androidandrew.sunscreen.ui.main

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.withFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.BaseUiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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

    /* TODO: Setup forecast
    @Test
    fun init_ifUvForecastDoesNotExist_trackingIsDisabled() {
        // TODO: Setup forecast
        onView(withId(R.id.trackingButton)).apply {
            check(matches(isNotEnabled()))
            check(matches(withText(R.string.start_tracking)))
        }
    }*/

    /* Disabled until user settings are no longer hardcoded
    @Test
    fun init_ifUserDoesNotExist_trackingIsDisabled() {
        // TODO: Setup user
        onView(withId(R.id.trackingButton)).apply {
            check(matches(isNotEnabled()))
            check(matches(withText(R.string.start_tracking)))
        }
    }*/

    @Test
    fun init_ifUserAndUvForecastExist_enablesStartTracking() {
        onView(withId(R.id.trackingButton)).apply {
            check(matches(isEnabled()))
            check(matches(withText(R.string.start_tracking)))
        }
    }

    @Test
    fun whenTrackingStarted_stopIsEnabled() {
        onView(withId(R.id.trackingButton)).perform(click())

        onView(withId(R.id.trackingButton)).apply {
            check(matches(isEnabled()))
            check(matches(withText(R.string.stop_tracking)))
        }
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