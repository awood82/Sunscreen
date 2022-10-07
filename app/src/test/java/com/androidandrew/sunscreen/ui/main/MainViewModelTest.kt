package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.util.getOrAwaitValue
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import java.io.IOException
//import org.robolectric.annotation.LooperMode
import java.time.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//@LooperMode(LooperMode.Mode.PAUSED)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: MainViewModel
    private val fakeUvService = FakeEpaService
    private val mockUvService = mockk<EpaService>()

    private fun createViewModel(useMock: Boolean = false, clock: Clock = FakeData.clockDefaultNoon) {
        vm = when (useMock) {
            true -> MainViewModel(mockUvService, clock)
            else -> MainViewModel(fakeUvService, clock)
        }
    }

    @After
    fun tearDown() {
        fakeUvService.exception = null
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
        createViewModel(clock = clock6pm)

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("No burn expected", burnTimeString)
    }

    @Test
    fun burnTimeString_ifNoNetworkConnection_isUnknown() {
        fakeUvService.exception = IOException()
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
        fakeUvService.exception = IOException()
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

    @Test
    fun networkError_triggersSnackbar() {
        fakeUvService.exception = IOException("Network error")
        createViewModel()

        assertEquals("Network error", vm.snackbarMessage.getOrAwaitValue())
    }

    @Test
    fun onLocationChanged_ifZip_isLessThan5Chars_doesNotRefreshNetwork() = runTest {
        createViewModel(useMock = true)

        vm.location = "1234"
        vm.onLocationChanged()

        coVerify(exactly=0) { mockUvService.getUvForecast("1234") }
    }

    @Test
    fun onLocationChanged_ifZip_isMoreThan5Chars_doesNotRefreshNetwork() = runTest {
        createViewModel(useMock = true)

        vm.location = "123456"
        vm.onLocationChanged()

        coVerify(exactly=0) { mockUvService.getUvForecast("123456") }
    }

    @Test
    fun onLocationChanged_ifZip_is5Digits_refreshesNetwork() = runTest {
        createViewModel(useMock = true)

        vm.location = "12345"
        vm.onLocationChanged()

        coVerify(exactly=1) { mockUvService.getUvForecast("12345") }
    }

    @Test
    fun onLocationChanged_ifZip_lengthIs5WithLettersPrefix_doesNotRefreshNetwork() = runTest {
        createViewModel(useMock = true)

        vm.location = "ABC45"
        vm.onLocationChanged()

        coVerify(exactly=0) { mockUvService.getUvForecast("ABC45") }
    }

    @Test
    fun onLocationChanged_ifZip_lengthIs5WithLettersPostfix_doesNotRefreshNetwork() = runTest {
        createViewModel(useMock = true)

        vm.location = "123DE"
        vm.onLocationChanged()

        coVerify(exactly=0) { mockUvService.getUvForecast("123DE") }
    }

    @Test
    fun onLocationChanged_ifZip_has5Digits_andSomeLetters_doesNotRefreshNetwork() = runTest {
        createViewModel(useMock = true)

        vm.location = "12345ABC"
        vm.onLocationChanged()

        coVerify(exactly=0) { mockUvService.getUvForecast("12345") }
        coVerify(exactly=0) { mockUvService.getUvForecast("12345ABC") }
    }
}