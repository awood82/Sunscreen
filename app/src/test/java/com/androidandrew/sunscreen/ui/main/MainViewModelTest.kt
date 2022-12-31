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
import org.junit.Before
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
    private lateinit var hourlyForecastRepo: HourlyForecastRepository
    private lateinit var userSettingsRepo: UserSettingsRepository
    private lateinit var userTrackingRepo: UserTrackingRepository
    private val locationUtil = LocationUtil()
    private val serviceController = mockk<SunTrackerServiceController>(relaxed = true)
    private val delta = 0.1

    @Before
    fun setup() {
        runBlocking {
            fakeDatabaseHolder.clearDatabase()
        }
        userTrackingRepo = UserTrackingRepositoryImpl(fakeDatabaseHolder.userTrackingDao)
        userSettingsRepo = UserSettingsRepositoryImpl(fakeDatabaseHolder.userSettingsDao)
    }

    @After
    fun tearDown() {
        fakeUvService.exception = null
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
    }

    private suspend fun createViewModel(useMockNetwork: Boolean = false, clock: Clock = FakeData.clockDefaultNoon) {
        this.clock = clock

        val networkToUse = when (useMockNetwork) {
            true -> mockUvService
            false -> fakeUvService
        }
        hourlyForecastRepo = HourlyForecastRepositoryImpl(fakeDatabaseHolder.hourlyForecastDao, networkToUse)

        vm = MainViewModel(
            getLocalForecastForToday = GetLocalForecastForTodayUseCase(
                userSettingsRepository = userSettingsRepo,
                hourlyForecastRepository = hourlyForecastRepo,
                clock = clock
            ),
            userSettingsRepo = userSettingsRepo,
            userTrackingRepo = userTrackingRepo,
            convertSpfUseCase = spfUseCase,
            sunburnCalculator = sunburnCalculator,
            locationUtil = locationUtil,
            clock = clock,
            sunTrackerServiceController = serviceController
        )
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
        createViewModel()
        fakeUvService.exception = IOException()

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

    @Test
    fun networkError_onInit_triggersSnackbar() = runTest {
        setLocation(FakeData.zip)
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
        createViewModel()

        updateTracking(0.0, 0.0)

        val tracking = userTrackingRepo.getUserTrackingFlow(getDate()).first()!!
        assertEquals(0.0, tracking.sunburnProgress, delta)
        assertEquals(0.0, tracking.vitaminDProgress, delta)
    }

    @Test
    fun forceTrackingRefresh_withArguments_updatesRepositoryValues() = runTest {
        createViewModel()

        updateTracking(1.0, 2.0)

        val tracking = userTrackingRepo.getUserTrackingFlow(getDate()).first()!!
        assertEquals(1.0, tracking.sunburnProgress, delta)
        assertEquals(2.0, tracking.vitaminDProgress, delta)
    }

    @Test
    fun forceTrackingRefresh_withArguments_andExistingRepoValue_updatesRepositoryValues() = runTest {
        setLocation(FakeData.zip)
        createViewModel()

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
        userSettingsRepo.setSpf(5)
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged(""))

        assertEquals(5, userSettingsRepo.getSpfFlow().first())
    }

    @Test
    fun onSpfTextChanged_toValidSpf_savesToRepo() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged("10"))

        assertEquals(10, userSettingsRepo.getSpfFlow().first())
    }

    @Test
    fun onSpfTextChanged_toTooHighSpf_stillSavesToRepo() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged("1000"))

        assertEquals(1000, userSettingsRepo.getSpfFlow().first())
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
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(true))

        assertTrue(userSettingsRepo.getIsOnSnowOrWaterFlow().first()!!)
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
        setLocation(FakeData.zip)
        createViewModel()

        // Check app state b/c it updates the location bar when the ViewModel first starts
        val appState = vm.appState.first()

        val locationBarState = vm.locationBarState.first()

        assertEquals(FakeData.zip, locationBarState.typedSoFar)
    }

    @Test
    fun init_ifLocationExistsInRepo_queriesRepoOnce() = runTest {
        setLocation(FakeData.zip)
        createViewModel(useMockNetwork = true)

        triggerLocationUpdate()

        coVerify(exactly = 1) { mockUvService.getUvForecast(FakeData.zip) }
    }

    @Test
    fun init_ifLocationExistsInRepo_isOnboarded() = runTest {
        setLocation(FakeData.zip)

        createViewModel()

        assertEquals(AppState.Onboarded, vm.appState.first())
    }

    @Test
    fun init_ifLocationDoesNotExistInRepo_isNotOnboarded() = runTest {

        createViewModel()

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

        assertEquals("10001", userSettingsRepo.getLocation())
    }

    @Test
    fun locationBarEvent_ifLocationSearched_andZipCodeIsInvalid_doesNotUpdateRepository() = runTest {
        createViewModel()

        vm.onLocationBarEvent(LocationBarEvent.LocationSearched("1"))

        assertNotEquals("10001", userSettingsRepo.getLocation())
    }


    private suspend fun updateTracking(burnProgress: Double, vitaminDProgress: Double) {
        val date = getDate()
        val userTracking = UserTracking(
            sunburnProgress = burnProgress,
            vitaminDProgress = vitaminDProgress
        )
        userTrackingRepo.setUserTracking(date, userTracking)
    }

    private fun getDate(): String {
        return LocalDate.now(clock).toString()
    }

    private suspend fun setLocation(location: String) {
        userSettingsRepo.setLocation(location)
    }

    private fun searchZip(zip: String) {
        vm.onLocationBarEvent(LocationBarEvent.TextChanged(zip))
        vm.onLocationBarEvent(LocationBarEvent.LocationSearched(zip))
        triggerLocationUpdate()
    }

    private fun triggerLocationUpdate() {
        runBlocking {
            val state = vm.uvChartUiState.first()
        }
    }
}