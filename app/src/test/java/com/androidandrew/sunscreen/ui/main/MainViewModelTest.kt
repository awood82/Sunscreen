package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.database.FakeDatabase
import com.androidandrew.sunscreen.network.FakeEpaService
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.util.FakeData
import com.androidandrew.sunscreen.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
//import org.robolectric.annotation.LooperMode
import java.time.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
//@LooperMode(LooperMode.Mode.PAUSED)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: MainViewModel
    private val fakeDatabaseHolder = FakeDatabase()
    private val fakeDatabase = fakeDatabaseHolder.createDatabase()
    private val fakeUvService = FakeEpaService()
    private val fakeRepository = SunscreenRepository(fakeDatabase, fakeUvService, FakeData.clockDefaultNoon)
    private val minEntries = 2 // minimum needed to calculate a time to burn

    private fun createViewModel(clock: Clock = FakeData.clockDefaultNoon) {
        vm = MainViewModel(fakeRepository, clock)
    }

    @Test
    fun burnTimeString_ifBurnExpected_isSet() = runTest {
        fakeDatabaseHolder.insertForecasts(12)
        createViewModel()

        // Accept any "<number> min" string
        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertTrue(burnTimeString.endsWith("min"))
        val firstChar = burnTimeString[0]
        assertTrue(firstChar.isDigit())
    }

    @Test
    fun burnTimeString_ifNoBurnExpected_isNotSet() = runTest {
        fakeDatabaseHolder.insertForecasts(minEntries)
        val clock6pm = Clock.offset(FakeData.clockDefaultNoon, Duration.ofHours(6))
        createViewModel(clock6pm)

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("No burn expected", burnTimeString)
    }

    @Test
    fun burnTimeString_ifNoNetworkConnection_isUnknown() = runTest {
        fakeUvService.simulateError()
        createViewModel()

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("Unknown", burnTimeString)
    }

    @Test
    fun burnTimeString_ifEntriesAreForTomorrow_isNighttime() = runTest {
        fakeUvService.simulateError()
        fakeDatabaseHolder.insertForecastsTomorrow(12)
        createViewModel()

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("Nighttime", burnTimeString)
    }

    @Test
    fun onTrackingClicked_canBeCalledMultipleTimes() {
        createViewModel()

        vm.onTrackingClicked()
        vm.onTrackingClicked()
        vm.onTrackingClicked()
    }

    @Test
    fun trackingButton_whenNoPredictionExists_isDisabled() = runTest {
        fakeUvService.simulateError()
        createViewModel()

        assertFalse(vm.isTrackingEnabled.getOrAwaitValue())
    }

    @Test
    fun trackingButton_whenPredictionExists_isEnabled() = runTest {
        fakeDatabaseHolder.insertForecasts(minEntries)
        createViewModel()

        assertTrue(vm.isTrackingEnabled.getOrAwaitValue())
    }
}