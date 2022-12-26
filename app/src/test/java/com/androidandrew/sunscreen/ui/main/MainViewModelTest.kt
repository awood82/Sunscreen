package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.database.UserTracking
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.testing.MainCoroutineRule
import com.androidandrew.sunscreen.util.LocationUtil
import com.androidandrew.sunscreen.testing.getOrAwaitValue
import com.androidandrew.sunscreen.ui.burntime.BurnTimeUiState
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.ui.location.LocationBarEvent
import com.androidandrew.sunscreen.ui.tracking.UvTrackingEvent
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
    private lateinit var realRepository: UserRepositoryImpl
    private val mockRepository = mockk<UserRepositoryImpl>(relaxed = true)
    private val locationUtil = LocationUtil()
    private val serviceController = mockk<SunTrackerServiceController>(relaxed = true)
    private var initDb = false
    private val delta = 0.1

    private suspend fun createViewModel(useMockNetwork: Boolean = false, useMockRepo: Boolean = false, clock: Clock = FakeData.clockDefaultNoon) {
        this.clock = clock
        fakeDatabaseHolder.clearDatabase()
        realRepository = UserRepositoryImpl(fakeDatabaseHolder.userTrackingDao, fakeDatabaseHolder.userSettingsDao)
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
        vm.onLocationBarEvent(LocationBarEvent.TextChanged(zip))
        vm.onSearchLocation(zip)
    }

    private fun setLocationToRefreshNetworkOnInit() {
        initDb = true
    }

    @Test
    fun burnTimeString_ifBurnExpected_isKnown() = runTest {
        createViewModel()

        searchZip(FakeData.zip)

        // Accept any "<number> min" string
        val burnTimeState = vm.burnTimeUiState.first()
        assertTrue(burnTimeState is BurnTimeUiState.Known)
    }

    @Test
    fun burnTimeState_ifNoBurnExpected_isBurnUnlikely() = runTest {
        val clock6pm = Clock.offset(FakeData.clockDefaultNoon, Duration.ofHours(6))
        createViewModel(clock = clock6pm)

        searchZip(FakeData.zip)

        val burnTimeState = vm.burnTimeUiState.first()
        assertTrue(burnTimeState is BurnTimeUiState.Unlikely)
    }

    @Test
    fun burnTimeState_ifNoNetworkConnection_isUnknown() = runTest {
        fakeUvService.exception = IOException()
        createViewModel()

        searchZip(FakeData.zip)

        val burnTimeState = vm.burnTimeUiState.first()
        assertTrue(burnTimeState is BurnTimeUiState.Unknown)
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

        assertFalse(vm.uvTrackingState.first().isTrackingPossible)
    }

    @Test
    fun trackingButton_whenPredictionExists_isEnabled() = runTest {
        createViewModel()

        searchZip(FakeData.zip)

        assertTrue(vm.uvTrackingState.first().isTrackingPossible)
    }

    @Test
    fun spf_whenChanged_updatesBurnEstimate() = runTest {
        createViewModel()
        searchZip(FakeData.zip)

        // The box starts with "1"
        val startingSpfState  = vm.burnTimeUiState.first()

        // Now it's changed to "15"
        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged("15"))
        advanceUntilIdle()

        val endingSpfState = vm.burnTimeUiState.first()
        assertNotEquals(startingSpfState, endingSpfState)
    }

    @Test
    fun onSnowOrWater_whenChanged_updatesBurnEstimate() = runTest {
        createViewModel()
        searchZip(FakeData.zip)

        // The box starts unchecked
        val startingBurnTimeState  = vm.burnTimeUiState.first()

        // Now it's checked
        vm.onUvTrackingEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(true))
        advanceUntilIdle()

        val endingBurnTimeState = vm.burnTimeUiState.first()
        assertNotEquals(startingBurnTimeState, endingBurnTimeState)
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
        var tracking = vm.uvTrackingState.first()
        assertEquals(10, tracking.sunburnProgressAmount)
        assertEquals(20, tracking.vitaminDProgressAmount)

        updateTracking(11.0, 22.0)
        advanceUntilIdle()
        tracking = vm.uvTrackingState.first()
        assertEquals(11, tracking.sunburnProgressAmount)
        assertEquals(22, tracking.vitaminDProgressAmount)
    }

    @Test
    fun onSpfChanged_whileNotTracking_doesNotUpdateController() = runTest {
        createViewModel()

        vm.onSpfChanged("5")

      verify(exactly = 0) { serviceController.setSpf(5) }
    }

    @Test
    fun onSpfChanged_whileTracking_updatesController() = runTest {
        createViewModel()
        vm.onTrackingClicked()

        vm.onSpfChanged("5")

        verify { serviceController.setSpf(5) }
    }

    @Test
    fun onIsSnowOrWaterChanged_whileNotTracking_doesNotUpdateController() = runTest {
        createViewModel()

        vm.onIsSnowOrWaterChanged(true)

        verify(exactly = 0) { serviceController.setIsOnSnowOrWater(true) }
    }

    @Test
    fun onIsSnowOrWaterChanged_whileTracking_updatesController() = runTest {
        createViewModel()
        vm.onTrackingClicked()

        vm.onIsSnowOrWaterChanged(true)

        verify { serviceController.setIsOnSnowOrWater(true) }
    }

    @Test
    fun afterSearch_ifUserAndUvForecastExist_enablesStartTracking() = runTest {
        createViewModel()

        searchZip(FakeData.zip)

        val trackingState = vm.uvTrackingState.first()
        assertTrue(trackingState.isTrackingPossible)
        assertFalse(trackingState.isTracking)
    }

    @Test
    fun whenTrackingStarted_stopIsEnabled() = runTest {
        createViewModel()

        searchZip(FakeData.zip)
        vm.onUvTrackingEvent(UvTrackingEvent.TrackingButtonClicked)

        val trackingState = vm.uvTrackingState.first()
        assertTrue(trackingState.isTrackingPossible)
        assertTrue(trackingState.isTracking)
    }

    @Test
    fun afterSearch_ifNetworkError_trackingIsDisabled() = runTest {
        fakeUvService.exception = IOException()
        createViewModel()

        searchZip(FakeData.zip)

        val trackingState = vm.uvTrackingState.first()
        assertFalse(trackingState.isTrackingPossible)
    }

    @Test
    fun init_ifLocationExistsInRepo_itAppearsInTheLocationBar() = runTest {
        setLocationInMockRepo(FakeData.zip)
        createViewModel(useMockRepo = true)

        val locationBarState = vm.locationBarState.first()

        assertEquals(FakeData.zip, locationBarState.typedSoFar)
    }

    @Test
    fun init_ifLocationExistsInRepo_queriesNetworkOnlyOnce() = runTest {
        setLocationInMockRepo(FakeData.zip)
        createViewModel(useMockNetwork = true, useMockRepo = true)

        coVerify(exactly = 1) { mockUvService.getUvForecast(FakeData.zip) }
    }

    @Test
    fun init_ifLocationDoesNotExistInRepo_navigatesToLocationScreen() = runTest {
        setLocationInMockRepo(null)

        createViewModel(useMockRepo = true)

        assertEquals(AppState.NotOnboarded, vm.appState.first())
    }

    @Test
    fun locationBarEvent_ifTextChanges_itIsUpdatedInViewModel() = runTest {
        createViewModel()

        vm.onLocationBarEvent(LocationBarEvent.TextChanged("10001"))

        val locationBarState = vm.locationBarState.first()
        assertEquals("10001", locationBarState.typedSoFar)
    }

    @Test
    fun locationBarEvent_ifLocationSearched_andZipCodeIsValid_updatesRepository() = runTest {
        createViewModel()

        vm.onLocationBarEvent(LocationBarEvent.LocationSearched("10001"))

        assertEquals("10001", realRepository.getLocation())
    }

    @Test
    fun locationBarEvent_ifLocationSearched_andZipCodeIsInvalid_doesNotUpdateRepository() = runTest {
        createViewModel()

        vm.onLocationBarEvent(LocationBarEvent.LocationSearched("1"))

        assertNotEquals("10001", realRepository.getLocation())
    }


    private fun setLocationInMockRepo(location: String?) {
        coEvery { mockRepository.getLocation() } returns location
        every { mockRepository.getLocationSync() } returns flowOf(location)
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