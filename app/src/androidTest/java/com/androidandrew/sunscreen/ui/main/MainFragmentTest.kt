package com.androidandrew.sunscreen.ui.main

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.withFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.BaseUiTest
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test

class MainFragmentTest: BaseUiTest() {

    private lateinit var fragmentScenario: FragmentScenario<MainFragment>

    @Before
    override fun setup() {
        super.setup()
        fragmentScenario = launchFragmentUnderTest(R.id.mainFragment)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        try {
            fragmentScenario.withFragment { requireActivity().stopLockTask() }
        } catch (e: Exception) {
        }
    }

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
    fun init_sunburnPercent_isDisplayed() {
        onView(withId(R.id.textSunburnProgress)).check(matches(withText(containsString(" %"))))
    }

    @Test
    fun init_vitaminD_IU_isDisplayed() {
        onView(withId(R.id.textVitaminDProgress)).check(matches(withText(containsString(" IU"))))
    }
}