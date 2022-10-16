package com.androidandrew.sunscreen.ui.location

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.withFragment
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.util.BaseUiTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LocationFragmentTest : BaseUiTest() {

    private lateinit var fragmentScenario: FragmentScenario<LocationFragment>

    @Before
    override fun setup() {
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

    private fun searchZip(zip: String = FakeData.zip) {
        onView(withId(R.id.editCurrentLocation))
            .perform(replaceText(zip))
        onView(withId(R.id.searchButton)).perform(click())
    }

    @Test
    fun searchButtonClick_whenZipIsInvalid_doesNotNavigate() {
        searchZip(zip = "123")

        assertEquals(R.id.locationFragment, navController.currentDestination?.id)
    }

    @Test
    fun searchButtonClick_whenZipIsValid_navigatesToMainScreen() {
        searchZip()

        assertEquals(R.id.mainFragment, navController.currentDestination?.id)
    }

    @Test
    fun navigatingBack_toLocationFragment_isDisabled() {
        searchZip()

        assertEquals(R.id.mainFragment, navController.currentDestination?.id)

        navController.navigateUp()
        assertEquals(R.id.mainFragment, navController.currentDestination?.id)
    }
}