package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabase
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.database.UserTracking
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.util.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
    private lateinit var clock: Clock
    private val fakeUvService = FakeEpaService
    private val mockUvService = mockk<EpaService>()
    private val fakeDatabaseHolder = FakeDatabase()
    private lateinit var realRepository: SunscreenRepository
    private val mockRepository = mockk<SunscreenRepository>(relaxed = true)
    private var initDb = false
    private val delta = 0.1

    private suspend fun createViewModel(useMockNetwork: Boolean = false, useMockRepo: Boolean = false, clock: Clock = FakeData.clockDefaultNoon) {
        this.clock = clock
        fakeDatabaseHolder.clearDatabase()
        realRepository = SunscreenRepository(fakeDatabaseHolder.db, clock)
        if (initDb) {
            realRepository.setLocation(FakeData.zip)
            coEvery { mockRepository.getLocation() } returns FakeData.zip
        }
        val networkToUse = when (useMockNetwork) {
            true -> mockUvService
            false -> fakeUvService
        }
        val repositoryToUse = when (useMockRepo) {
            true -> mockRepository
            false -> realRepository
        }
        vm = MainViewModel(networkToUse, repositoryToUse, clock)
    }

    @After
    fun tearDown() {
        fakeUvService.exception = null
        fakeDatabaseHolder.tearDown()
    }

    private fun searchZip(zip: String) {
        vm.locationEditText.value = zip
        vm.onSearchLocation()
    }

    private fun setLocationToRefreshNetworkOnInit() {
        initDb = true
    }

    @Test
    fun burnTimeString_ifBurnExpected_isSet() = runTest {
        createViewModel()

        searchZip(FakeData.zip)

        // Accept any "<number> min" string
        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertTrue(burnTimeString.endsWith("min"))
        val firstChar = burnTimeString[0]
        assertTrue(firstChar.isDigit())
    }

    @Test
    fun burnTimeString_ifNoBurnExpected_isNotSet() = runTest {
        val clock6pm = Clock.offset(FakeData.clockDefaultNoon, Duration.ofHours(6))
        createViewModel(clock = clock6pm)

        searchZip(FakeData.zip)

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("No burn expected", burnTimeString)
    }

    @Test
    fun burnTimeString_ifNoNetworkConnection_isUnknown() = runTest {
        fakeUvService.exception = IOException()
        createViewModel()

        searchZip(FakeData.zip)

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("Unknown", burnTimeString)
    }

    @Test
    fun onTrackingClicked_canBeCalledMultipleTimes() = runTest {
        createViewModel()

        vm.onTrackingClicked()
        vm.onTrackingClicked()
        vm.onTrackingClicked()
    }

    @Test
    fun trackingButton_whenNoPredictionExists_isDisabled() = runTest {
        fakeUvService.exception = IOException()
        createViewModel()

        assertFalse(vm.isTrackingEnabled.getOrAwaitValue())
    }

    @Test
    fun trackingButton_whenPredictionExists_isEnabled() = runTest {
        createViewModel()

        searchZip(FakeData.zip)

        assertTrue(vm.isTrackingEnabled.getOrAwaitValue())
    }

    @Test
    fun onSnowOrWaterChanged_togglesValue() = runTest {
        createViewModel()
        searchZip(FakeData.zip)

        val startingValue = vm.isOnSnowOrWater
        vm.onSnowOrWaterChanged()
        assertEquals(!startingValue, vm.isOnSnowOrWater)

        vm.onSnowOrWaterChanged()
        assertEquals(startingValue, vm.isOnSnowOrWater)
    }

    @Test
    fun onSnowOrWaterChanged_changesBurnEstimate() = runTest {
        createViewModel()
        searchZip(FakeData.zip)
        // Assumes that the box starts unchecked
        val startingBurnTime = vm.burnTimeString.getOrAwaitValue()

        vm.onSnowOrWaterChanged()
        val endingBurnTime = vm.burnTimeString.getOrAwaitValue()

        assertNotEquals(startingBurnTime, endingBurnTime)
    }

    @Test
    fun getSpfClamped_clampsBetween1and50() = runTest {
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
    fun networkError_onSearch_triggersSnackbar() = runTest {
        fakeUvService.exception = IOException("Network error")
        createViewModel()

        searchZip("12345")

        assertEquals("Network error", vm.snackbarMessage.getOrAwaitValue())
    }

    // TODO: onInit tests not working. Hang in database get()
    @Test
    fun networkError_onInit_triggersSnackbar() = runTest {
        setLocationToRefreshNetworkOnInit()
        fakeUvService.exception = IOException("Network error")
        createViewModel()

        assertEquals("Network error", vm.snackbarMessage.getOrAwaitValue())
    }

    @Test
    fun onLocationChanged_ifZip_isLessThan5Chars_doesNotRefreshNetwork() = runTest {
        createViewModel(useMockNetwork = true)

        searchZip("1234")

        coVerify(exactly=0) { mockUvService.getUvForecast("1234") }
    }

    @Test
    fun onLocationChanged_ifZip_isMoreThan5Chars_doesNotRefreshNetwork() = runTest {
        createViewModel(useMockNetwork = true)

        searchZip("123456")

        coVerify(exactly=0) { mockUvService.getUvForecast("123456") }
    }

    @Test
    fun onLocationChanged_ifZip_is5Digits_refreshesNetwork() = runTest {
        createViewModel(useMockNetwork = true)

        searchZip("12345")

        coVerify(exactly=1) { mockUvService.getUvForecast("12345") }
    }

    @Test
    fun onLocationChanged_ifZip_lengthIs5WithLettersPrefix_doesNotRefreshNetwork() = runTest {
        createViewModel(useMockNetwork = true)

        searchZip("ABC45")

        coVerify(exactly=0) { mockUvService.getUvForecast("ABC45") }
    }

    @Test
    fun onLocationChanged_ifZip_lengthIs5WithLettersPostfix_doesNotRefreshNetwork() = runTest {
        createViewModel(useMockNetwork = true)

        searchZip("123DE")

        coVerify(exactly=0) { mockUvService.getUvForecast("123DE") }
    }

    @Test
    fun onLocationChanged_ifZip_has5Digits_andSomeLetters_doesNotRefreshNetwork() = runTest {
        createViewModel(useMockNetwork = true)

        searchZip("12345ABC")

        coVerify(exactly=0) { mockUvService.getUvForecast("12345") }
        coVerify(exactly=0) { mockUvService.getUvForecast("12345ABC") }
    }

    @Test
    fun forceTrackingRefresh_withNoPreviousTrackingInfo_triggersRepositoryUpdate() = runTest {
        coEvery { mockRepository.getUserTrackingInfo(any()) } returns null
        createViewModel(useMockNetwork = false, useMockRepo = true)

        vm.forceTrackingRefresh()

        coVerify { mockRepository.setUserTrackingInfo(any()) }
    }

    @Test
    fun forceTrackingRefresh_withArguments_updatesRepositoryValues() = runTest {
        coEvery { mockRepository.getUserTrackingInfo(any()) } returns null
        createViewModel(useMockNetwork = false, useMockRepo = true)

        vm.forceTrackingRefresh(1.0, 2.0)

        val slot = slot<UserTracking>()
        coVerify { mockRepository.setUserTrackingInfo(capture(slot)) }
        assertEquals(1.0, slot.captured.burnProgress, delta)
        assertEquals(2.0, slot.captured.vitaminDProgress, delta)
    }

    @Test
    fun forceTrackingRefresh_withArguments_andExistingRepoValue_updatesRepositoryValues() = runTest {
        createViewModel(useMockNetwork = false, useMockRepo = true)
        val userInfo = UserTracking(date = LocalDate.now(clock).toString(), burnProgress = 10.0, vitaminDProgress = 20.0)
        coEvery { mockRepository.getUserTrackingInfo(any()) } returns userInfo

        vm.forceTrackingRefresh(1.0, 2.0)

        val slot = slot<UserTracking>()
        coVerify { mockRepository.setUserTrackingInfo(capture(slot)) }
        assertEquals(11.0, slot.captured.burnProgress, delta)
        assertEquals(22.0, slot.captured.vitaminDProgress, delta)
    }
}