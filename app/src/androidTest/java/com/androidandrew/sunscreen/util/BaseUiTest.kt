package com.androidandrew.sunscreen.util

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import com.androidandrew.sunscreen.R
import org.junit.After

@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class BaseUiTest {

//    lateinit var navController: NavController

    @Before
    open fun setup() {
//        TestUtil.clearDatabase()
        setupNavController()
    }

    @After
    open fun tearDown() {
        // Do nothing
    }

    private fun setupNavController() {
        /*navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        runOnUiThread {
            navController.setGraph(R.navigation.navigation)
        }*/
    }

    protected inline fun <reified T: Fragment> launchFragmentUnderTest(): FragmentScenario<T> {
        val scenario = launchFragmentInContainer<T>(themeResId = R.style.Theme_Sunscreen)
  /*      scenario.withFragment {
            try {
                Navigation.setViewNavController(this.requireView(), navController)
            } catch (e: IllegalStateException) {

            }
        }
        */
        runBlocking { delay(100) } // account for initialization in viewModelScope
        return scenario
    }
/*
    protected inline fun <reified T: DialogFragment> launchDialogFragmentUnderTest(): FragmentScenario<T> {
        val scenario = with (launchFragment<T>(themeResId = R.style.AppTheme)) {
            onFragment { fragment ->
                assertNotNull(fragment.dialog)
            }
        }
        return scenario
    }
*/
    protected fun openOptionsMenu() {
        try {
            Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        } catch (e: Exception) {
            // Ignore
        }
    }
}