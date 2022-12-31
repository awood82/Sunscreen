package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.*
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.domain.ConvertSpfUseCase
import com.androidandrew.sunscreen.domain.usecases.GetLocalForecastForTodayUseCase
import com.androidandrew.sunscreen.domain.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.model.UserTracking
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.testing.MainCoroutineRule
import com.androidandrew.sunscreen.util.LocationUtil
import com.androidandrew.sunscreen.testing.getOrAwaitValue
import com.androidandrew.sunscreen.ui.burntime.BurnTimeUiState
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
    private val spfUseCase = ConvertSpfUseCase()
    private val sunburnCalculator = SunburnCalculator(spfUseCase)
    private lateinit var clock: Clock
    private val fakeUvService = FakeEpaService
    private val mockUvService = mockk<EpaService>()
    private val fakeDatabaseHolder = FakeDatabaseWrapper()
    private lateinit var realHourlyForecastRepository: HourlyForecastRepository
    private lateinit var realUserSettingsRepository: UserSettingsRepository
    private val mockUserSettingsRepository = mockk<UserSettingsRepository>(relaxed = true)
    private lateinit var realUserTrackingRepository: UserTrackingRepository
    private val mockUserTrackingRepository = mockk<UserTrackingRepository>(relaxed = true)
    private val locationUtil = LocationUtil()
    private val serviceController = mockk<SunTrackerServiceController>(relaxed = true)
    private var initDb = false
    private val delta = 0.1

    private suspend fun createViewModel(useMockNetwork: Boolean = false, useMockRepos: Boolean = false, clock: Clock = FakeData.clockDefaultNoon) {
        this.clock = clock
        fakeDatabaseHolder.clearDatabase()
        val networkToUse = when (useMockNetwork) {
            true -> mockUvService
            false -> fakeUvService
        }
        realUserTrackingRepository = UserTrackingRepositoryImpl(fakeDatabaseHolder.userTrackingDao)
        realUserSettingsRepository = UserSettingsRepositoryImpl(fakeDatabaseHolder.userSettingsDao)
        realHourlyForecastRepository = HourlyForecastRepositoryImpl(fakeDatabaseHolder.hourlyForecastDao, networkToUse)

        coEvery { mockUserSettingsRepository.getSpf() } returns null
        if (initDb) {
            realUserSettingsRepository.setLocation(FakeData.zip)
            coEvery { mockUserSettingsRepository.getLocation() } returns FakeData.zip
        } else {
            coEvery { mockUserSettingsRepository.getLocation() } returns ""
        }

        val userTrackingRepositoryToUse = when (useMockRepos) {
            true -> mockUserTrackingRepository
            false -> realUserTrackingRepository
        }
        val userSettingsRepositoryToUse = when (useMockRepos) {
            true -> mockUserSettingsRepository
            false -> realUserSettingsRepository
        }
        // no mockHourlyForecastRepository needed. Tests use useMockRepos for testing user tracking and settings only
        val hourlyForecastRepositoryToUse = realHourlyForecastRepository

        vm = MainViewModel(
            getLocalForecastForToday = GetLocalForecastForTodayUseCase(
                userSettingsRepository = userSettingsRepositoryToUse,
                hourlyForecastRepository = hourlyForecastRepositoryToUse,
                clock = clock
            ),
            userSettingsRepo = userSettingsRepositoryToUse,
            userTrackingRepo = userTrackingRepositoryToUse,
            convertSpfUseCase = spfUseCase,
            sunburnCalculator = sunburnCalculator,
            locationUtil = locationUtil,
            clock = clock,
            sunTrackerServiceController = serviceController
        )
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
        vm.onLocationBarEvent(LocationBarEvent.LocationSearched(zip))
        triggerLocationUpdate()
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

        // It should update based on no sunscreen when the SPF box is cleared
        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged(""))
        advanceUntilIdle()

        val emptySpfState = vm.burnTimeUiState.first()
        assertEquals(startingSpfState, emptySpfState)
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
        coEvery { mockUserTrackingRepository.getUserTracking(any()) } returns null
        createViewModel(useMockNetwork = false, useMockRepos = true)

        updateTracking(0.0, 0.0)

        coVerify { mockUserTrackingRepository.setUserTracking(any(), any()) }
    }

    @Test
    fun forceTrackingRefresh_withArguments_updatesRepositoryValues() = runTest {
        coEvery { mockUserTrackingRepository.getUserTracking(any()) } returns null
        createViewModel(useMockNetwork = false, useMockRepos = true)

        updateTracking(1.0, 2.0)

        val slot = slot<UserTracking>()
        coVerify { mockUserTrackingRepository.setUserTracking(any(), capture(slot)) }
        assertEquals(1.0, slot.captured.sunburnProgress, delta)
        assertEquals(2.0, slot.captured.vitaminDProgress, delta)
    }

    @Test
    fun forceTrackingRefresh_withArguments_andExistingRepoValue_updatesRepositoryValues() = runTest {
        initDb = true
        createViewModel(useMockNetwork = false, useMockRepos = false)

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
    fun onSpfTextChanged_toEmptyString_doesNotSaveToRepo() = runTest {
        createViewModel(useMockRepos = true)

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged(""))

        coVerify(exactly = 0) { mockUserSettingsRepository.setSpf(any()) }
    }

    @Test
    fun onSpfTextChanged_toValidSpf_savesToRepo() = runTest {
        createViewModel(useMockRepos = true)

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged("10"))

        coVerify(exactly = 1) { mockUserSettingsRepository.setSpf(10) }
    }

    @Test
    fun onSpfTextChanged_toTooHighSpf_stillSavesToRepo() = runTest {
        createViewModel(useMockRepos = true)

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged("1000"))

        coVerify(exactly = 1) { mockUserSettingsRepository.setSpf(1000) }
    }

    @Test
    fun onSpfChanged_whileNotTracking_doesNotUpdateController() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged("5"))

        verify(exactly = 0) { serviceController.setSpf(5) }
    }

    @Test
    fun onSpfChanged_toInvalidValue_whileTracking_doesNotUpdateController() = runTest {
        createViewModel()
        vm.onTrackingClicked()

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged(""))

        verify(exactly = 0) { serviceController.setSpf(any()) }
    }

    @Test
    fun onSpfChanged_whileTracking_updatesController() = runTest {
        createViewModel()
        vm.onTrackingClicked()

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged("5"))

        verify { serviceController.setSpf(5) }
    }

    @Test
    fun onIsSnowOrWaterChanged_whileNotTracking_doesNotUpdateController() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(true))

        verify(exactly = 0) { serviceController.setIsOnSnowOrWater(true) }
    }

    @Test
    fun onIsSnowOrWaterChanged_whileTracking_updatesController() = runTest {
        createViewModel()
        vm.onTrackingClicked()

        vm.onUvTrackingEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(true))

        verify { serviceController.setIsOnSnowOrWater(true) }
    }

    @Test
    fun onIsOnSnowOrWaterChanged_savesToRepo() = runTest {
        createViewModel(useMockRepos = true)

        vm.onUvTrackingEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(true))

        coVerify(exactly = 1) { mockUserSettingsRepository.setIsOnSnowOrWater(true) }
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
        createViewModel(useMockRepos = true)

        // Check app state b/c it updates the location bar when the ViewModel first starts
        val appState = vm.appState.first()

        val locationBarState = vm.locationBarState.first()

        assertEquals(FakeData.zip, locationBarState.typedSoFar)
    }

    @Test
    fun init_ifLocationExistsInRepo_queriesRepoOnce() = runTest {
        setLocationInMockRepo(FakeData.zip)
        createViewModel(useMockNetwork = true, useMockRepos = true)

        triggerLocationUpdate()

        coVerify(exactly = 1) { mockUvService.getUvForecast(FakeData.zip) }
    }

    @Test
    fun init_ifLocationExistsInRepo_isOnboarded() = runTest {
        setLocationInMockRepo(FakeData.zip)

        createViewModel(useMockRepos = true)

        assertEquals(AppState.Onboarded, vm.appState.first())
    }

    @Test
    fun init_ifLocationDoesNotExistInRepo_isNotOnboarded() = runTest {
        setLocationInMockRepo(null)

        createViewModel(useMockRepos = true)

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

        assertEquals("10001", realUserSettingsRepository.getLocation())
    }

    @Test
    fun locationBarEvent_ifLocationSearched_andZipCodeIsInvalid_doesNotUpdateRepository() = runTest {
        createViewModel()

        vm.onLocationBarEvent(LocationBarEvent.LocationSearched("1"))

        assertNotEquals("10001", realUserSettingsRepository.getLocation())
    }


    private fun setLocationInMockRepo(location: String?) {
        coEvery { mockUserSettingsRepository.getLocation() } returns location
        every { mockUserSettingsRepository.getLocationFlow() } returns flowOf(location)
    }

    private suspend fun updateTracking(burnProgress: Double, vitaminDProgress: Double) {
        val date = LocalDate.now(clock).toString()
        val userTracking = UserTracking(
            sunburnProgress = burnProgress,
            vitaminDProgress = vitaminDProgress
        )
        mockUserTrackingRepository.setUserTracking(date, userTracking)
        realUserTrackingRepository.setUserTracking(date, userTracking)
    }

    private fun triggerLocationUpdate() {
        runBlocking {
            val state = vm.uvChartUiState.first()
        }
    }
}