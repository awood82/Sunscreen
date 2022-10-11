package com.androidandrew.sunscreen.ui.main

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.withFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabase
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.database.UserSetting
import com.androidandrew.sunscreen.database.UserSettingsDao
import com.androidandrew.sunscreen.util.BaseUiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MainFragmentWithNetworkErrorTest : BaseUiTest() {

    private lateinit var fragmentScenario: FragmentScenario<MainFragment>
    private val errorMessage = "generic error"

    @Before
    override fun setup() {
        // NOTE on fixing pinned emulator: adb shell am task lock stop
        super.setup()

        FakeEpaService.exception = IOException(errorMessage)
        FakeDatabase().db.userSettingsDao.insert(
            UserSetting(UserSettingsDao.LOCATION, FakeData.zip)
        )

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
    fun init_ifNetworkError_trackingIsDisabled() {
        onView(withId(R.id.trackingButton)).apply {
            check(matches(isNotEnabled()))
            check(matches(withText(R.string.start_tracking)))
        }
    }

    @Test
    fun init_ifNetworkError_showsErrorSnackbar() {
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(errorMessage)))
    }
}