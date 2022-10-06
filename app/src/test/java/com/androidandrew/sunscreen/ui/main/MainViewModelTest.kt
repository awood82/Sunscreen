package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.util.getOrAwaitValue
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
//import org.robolectric.annotation.LooperMode
import java.time.*

@RunWith(AndroidJUnit4::class)
//@LooperMode(LooperMode.Mode.PAUSED)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: MainViewModel
    private val fakeUvService = FakeEpaService()

    private fun createViewModel(clock: Clock = FakeData.clockDefaultNoon) {
        vm = MainViewModel(fakeUvService, clock)
    }

    @Test
    fun burnTimeString_ifBurnExpected_isSet() {
        createViewModel()

        // Accept any "<number> min" string
        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertTrue(burnTimeString.endsWith("min"))
        val firstChar = burnTimeString[0]
        assertTrue(firstChar.isDigit())
    }

    @Test
    fun burnTimeString_ifNoBurnExpected_isNotSet() {
        val clock6pm = Clock.offset(FakeData.clockDefaultNoon, Duration.ofHours(6))
        createViewModel(clock6pm)

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("No burn expected", burnTimeString)
    }

    @Test
    fun burnTimeString_ifNoNetworkConnection_isUnknown() {
        fakeUvService.simulateError()
        createViewModel()

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("Unknown", burnTimeString)
    }

    @Test
    fun onTrackingClicked_canBeCalledMultipleTimes() {
        createViewModel()

        vm.onTrackingClicked()
        vm.onTrackingClicked()
        vm.onTrackingClicked()
    }

    @Test
    fun trackingButton_whenNoPredictionExists_isDisabled() {
        fakeUvService.simulateError()
        createViewModel()

        assertFalse(vm.isTrackingEnabled.getOrAwaitValue())
    }

    @Test
    fun trackingButton_whenPredictionExists_isEnabled() {
        createViewModel()

        assertTrue(vm.isTrackingEnabled.getOrAwaitValue())
    }

    @Test
    fun onSnowOrWaterChanged_togglesValue() {
        createViewModel()
        val startingValue = vm.isOnSnowOrWater

        vm.onSnowOrWaterChanged()
        assertEquals(!startingValue, vm.isOnSnowOrWater)

        vm.onSnowOrWaterChanged()
        assertEquals(startingValue, vm.isOnSnowOrWater)
    }

    @Test
    fun onSnowOrWaterChanged_changesBurnEstimate() {
        createViewModel()
        // Assumes that the box starts unchecked
        val startingBurnTime = vm.burnTimeString.getOrAwaitValue()

        vm.onSnowOrWaterChanged()
        val endingBurnTime = vm.burnTimeString.getOrAwaitValue()

        assertNotEquals(startingBurnTime, endingBurnTime)
    }

    @Test
    fun getSpfClamped_clampsBetween1and50() {
        createViewModel()

        vm.spf = "0"
        assertEquals(1, vm.getSpfClamped())

        vm.spf = "1"
        assertEquals(1, vm.getSpfClamped())

        vm.spf = "-1"
        assertEquals(1, vm.getSpfClamped())

        vm.spf = ""
        assertEquals(1, vm.getSpfClamped())

        vm.spf = "50"
        assertEquals(50, vm.getSpfClamped())

        vm.spf = "51"
        assertEquals(50, vm.getSpfClamped())
    }
}