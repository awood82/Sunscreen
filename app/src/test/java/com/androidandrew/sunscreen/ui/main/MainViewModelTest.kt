package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.database.UserTracking
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.util.LocationUtil
import com.androidandrew.sunscreen.util.MainCoroutineRule
import com.androidandrew.sunscreen.util.getOrAwaitValue
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//@LooperMode(LooperMode.Mode.PAUSED)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var vm: MainViewModel
    private lateinit var clock: Clock
    private val fakeUvService = FakeEpaService
    private val mockUvService = mockk<EpaService>()
    private val fakeDatabaseHolder = FakeDatabaseWrapper()
    private lateinit var realRepository: SunscreenRepository
    private val mockRepository = mockk<SunscreenRepository>(relaxed = true)
    private val locationUtil = LocationUtil()
    private val serviceController = mockk<SunTrackerServiceController>(relaxed = true)
    private var initDb = false
    private val delta = 0.1

    private suspend fun createViewModel(useMockNetwork: Boolean = false, useMockRepo: Boolean = false, clock: Clock = FakeData.clockDefaultNoon) {
        this.clock = clock
        fakeDatabaseHolder.clearDatabase()
        realRepository = SunscreenRepository(fakeDatabaseHolder.db)
        if (initDb) {
            realRepository.setLocation(FakeData.zip)
            coEvery { mockRepository.getLocation() } returns FakeData.zip
        } else {
            coEvery { mockRepository.getLocation() } returns ""
        }
        val networkToUse = when (useMockNetwork) {
            true -> mockUvService
            false -> fakeUvService
        }
        val repositoryToUse = when (useMockRepo) {
            true -> mockRepository
            false -> realRepository
        }
        vm = MainViewModel(networkToUse, repositoryToUse, locationUtil, clock, serviceController)
    }

    @After
    fun tearDown() {
        fakeUvService.exception = null
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
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
        val burnTimeString = vm.burnTimeString.first()
        assertTrue(burnTimeString.endsWith("min"))
        val firstChar = burnTimeString[0]
        assertTrue(firstChar.isDigit())
    }

    @Test
    fun burnTimeString_ifNoBurnExpected_isNotSet() = runTest {
        val clock6pm = Clock.offset(FakeData.clockDefaultNoon, Duration.ofHours(6))
        createViewModel(clock = clock6pm)

        searchZip(FakeData.zip)

        val burnTimeString = vm.burnTimeString.first()
        assertEquals("No burn expected", burnTimeString)
    }

    @Test
    fun burnTimeString_ifNoNetworkConnection_isUnknown() = runTest {
        fakeUvService.exception = IOException()
        createViewModel()

        searchZip(FakeData.zip)

        val burnTimeString = vm.burnTimeString.value//getOrAwaitValue()
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

        assertFalse(vm.isTrackingEnabled.value)//getOrAwaitValue())
    }

    @Test
    fun trackingButton_whenPredictionExists_isEnabled() = runTest {
        createViewModel()

        searchZip(FakeData.zip)

        assertTrue(vm.isTrackingEnabled.first())
    }

    @Test
    fun onSnowOrWaterChanged_changesBurnEstimate() = runTest {
        createViewModel()
        searchZip(FakeData.zip)

        // The box starts unchecked
        val startingBurnTime = vm.burnTimeString.first()

        // Now it's checked
        vm.isOnSnowOrWater.value = true
        advanceUntilIdle()

        val endingBurnTime = vm.burnTimeString.first()
        assertNotEquals(startingBurnTime, endingBurnTime)
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

        updateTracking(0.0, 0.0)

        coVerify { mockRepository.setUserTrackingInfo(any()) }
    }

    @Test
    fun forceTrackingRefresh_withArguments_updatesRepositoryValues() = runTest {
        coEvery { mockRepository.getUserTrackingInfo(any()) } returns null
        createViewModel(useMockNetwork = false, useMockRepo = true)

        updateTracking(1.0, 2.0)

        val slot = slot<UserTracking>()
        coVerify { mockRepository.setUserTrackingInfo(capture(slot)) }
        assertEquals(1.0, slot.captured.burnProgress, delta)
        assertEquals(2.0, slot.captured.vitaminDProgress, delta)
    }

    @Test
    fun forceTrackingRefresh_withArguments_andExistingRepoValue_updatesRepositoryValues() = runTest {
        initDb = true
        createViewModel(useMockNetwork = false, useMockRepo = false)

        updateTracking(10.0, 20.0)
        advanceUntilIdle()
        assertEquals(10.0, vm.sunUnitsToday.first(), delta)
        assertEquals(20.0, vm.vitaminDUnitsToday.first(), delta)

        updateTracking(11.0, 22.0)
        advanceUntilIdle()
        assertEquals(11.0, vm.sunUnitsToday.first(), delta)
        assertEquals(22.0, vm.vitaminDUnitsToday.first(), delta)
    }

    private suspend fun updateTracking(burnProgress: Double, vitaminDProgress: Double) {
        val userTracking = UserTracking(
            date = LocalDate.now(clock).toString(),
            burnProgress = burnProgress,
            vitaminDProgress = vitaminDProgress
        )
        mockRepository.setUserTrackingInfo(userTracking)
        realRepository.setUserTrackingInfo(userTracking)
    }
}