package com.androidandrew.sunscreen.util

import androidx.annotation.NavigationRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.network.FakeEpaService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import com.androidandrew.sunscreen.R
import org.junit.After
import org.junit.Assert.assertNotNull

@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class BaseUiTest {

    lateinit var navController: NavController

    @Before
    open fun setup() {
        createNavController()
        setNavGraph()
//        FakeDatabase().clearDatabase()
    }

    @After
    open fun tearDown() {
        FakeEpaService.exception = null
        runBlocking {
            FakeDatabaseWrapper().tearDown()
        }
    }

    private fun createNavController() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    private fun setNavGraph() {
        runOnUiThread {
            navController.setGraph(R.navigation.navigation)
        }
    }

    protected inline fun <reified T: Fragment> launchFragmentUnderTest(@NavigationRes initialDestinationId: Int): FragmentScenario<T> {
        val scenario = launchFragmentInContainer<T>(themeResId = R.style.Theme_Sunscreen)
        scenario.withFragment {
            try {
                (navController as TestNavHostController).setCurrentDestination(initialDestinationId)
                Navigation.setViewNavController(this.requireView(), navController)
            } catch (e: IllegalStateException) {

            }
        }
        runBlocking { delay(100) } // account for initialization in viewModelScope
        return scenario
    }

    protected inline fun <reified T: DialogFragment> launchDialogFragmentUnderTest(@NavigationRes initialDestinationId: Int): FragmentScenario<T> {
        val scenario = with (launchFragment<T>(themeResId = R.style.Theme_Sunscreen)) {
            (navController as TestNavHostController).setCurrentDestination(initialDestinationId)
            onFragment { fragment ->
                assertNotNull(fragment.dialog)
            }
        }
        return scenario
    }

    protected fun openOptionsMenu() {
        try {
            Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        } catch (e: Exception) {
            // Ignore
        }
    }
}