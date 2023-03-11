package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.analytics.EventLogger
import com.androidandrew.sunscreen.common.DataResult
import com.androidandrew.sunscreen.data.repository.*
import com.androidandrew.sunscreen.domain.ConvertSpfUseCase
import com.androidandrew.sunscreen.domain.usecases.GetLocalForecastForTodayUseCase
import com.androidandrew.sunscreen.domain.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.model.UserTracking
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.testing.MainDispatcherRule
import com.androidandrew.sunscreen.util.LocationUtil
import com.androidandrew.sunscreen.ui.burntime.BurnTimeUiState
import com.androidandrew.sunscreen.ui.chart.UvChartUiState
import com.androidandrew.sunscreen.ui.location.LocationBarEvent
import com.androidandrew.sunscreen.ui.navigation.AppDestination
import com.androidandrew.sunscreen.ui.tracking.UvTrackingEvent
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import java.io.IOException
import java.time.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//@LooperMode(LooperMode.Mode.PAUSED)
class MainViewModelTest {

    private annotation class IsNotOnboarded

    @get:Rule
    val testName = TestName()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var vm: MainViewModel
    private val spfUseCase = ConvertSpfUseCase()
    private val sunburnCalculator = SunburnCalculator(spfUseCase)
    private lateinit var clock: Clock
    private val fakeUvService = FakeEpaService
    private val fakeDatabaseHolder = FakeDatabaseWrapper()
    private lateinit var hourlyForecastRepo: HourlyForecastRepository
    private lateinit var userSettingsRepo: UserSettingsRepository
    private lateinit var userTrackingRepo: UserTrackingRepository
    private val locationUtil = LocationUtil()
    private val serviceController = mockk<SunTrackerServiceController>(relaxed = true)
    private val mockAnalytics: EventLogger = mockk(relaxed = true)
    private val delta = 0.1
    private lateinit var chartState: UvChartUiState
    private lateinit var collectJob: Job

    @Before
    fun setup() {
        runBlocking {
            fakeDatabaseHolder.clearDatabase()
        }
        userTrackingRepo = UserTrackingRepositoryImpl(fakeDatabaseHolder.userTrackingDao)
        userSettingsRepo = UserSettingsRepositoryImpl(fakeDatabaseHolder.userSettingsDao)
        hourlyForecastRepo = HourlyForecastRepositoryImpl(fakeDatabaseHolder.hourlyForecastDao, fakeUvService)
    }

    @After
    fun tearDown() {
        fakeUvService.exception = null
        runBlocking {
            fakeDatabaseHolder.tearDown()
        }
    }

    private suspend fun createViewModel(clock: Clock = FakeData.clockDefaultNoon) {
        this.clock = clock

        val method = this.javaClass.getMethod(testName.methodName)
        val notOnboarded = method.isAnnotationPresent(IsNotOnboarded::class.java)
        runBlocking {
            userSettingsRepo.setIsOnboarded(!notOnboarded)
        }

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
            sunTrackerServiceController = serviceController,
            analytics = mockAnalytics
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

        vm.onUvTrackingEvent(UvTrackingEvent.TrackingButtonClicked)
        vm.onUvTrackingEvent(UvTrackingEvent.TrackingButtonClicked)
        vm.onUvTrackingEvent(UvTrackingEvent.TrackingButtonClicked)
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
    fun networkError_onSearch_triggersError() = runTest {
        fakeUvService.exception = IOException("Network error")
        createViewModel()

        searchZip("12345")

        val forecast = vm.forecastState.first()
        assertEquals("Network error", (forecast as ForecastState.Error).message)
    }

    @Test
    fun networkError_onInit_triggersError() = runTest {
        fakeUvService.exception = IOException("Network error")
        setLocation(FakeData.zip)
        createViewModel()

        triggerLocationUpdate()

        val forecast = vm.forecastState.first()
        assertEquals("Network error", (forecast as ForecastState.Error).message)
    }

    @Test
    fun onLocationChanged_ifZip_isLessThan5Chars_doesNotRefreshNetwork() = runTest {
        val tooShortZip = "1234"
        createViewModel()

        searchZip(tooShortZip)

        assertTrue(chartState is UvChartUiState.NoData)
    }

    @Test
    fun onLocationChanged_ifZip_isMoreThan5Chars_doesNotRefreshNetwork() = runTest {
        val tooLongZip = "123456"
        createViewModel()

        searchZip(tooLongZip)

        assertTrue(chartState is UvChartUiState.NoData)
    }

    @Test
    fun onLocationChanged_ifZip_is5Digits_refreshesNetwork() = runTest {
        val justRightZip = "12345"
        createViewModel()

        searchZip(justRightZip)

        assertTrue(chartState is UvChartUiState.HasData)
    }

    @Test
    fun onLocationChanged_ifZip_lengthIs5WithLettersPrefix_doesNotRefreshNetwork() = runTest {
        val alphaZip = "ABC45"
        createViewModel()

        searchZip(alphaZip)

        assertTrue(chartState is UvChartUiState.NoData)
    }

    @Test
    fun onLocationChanged_ifZip_lengthIs5WithLettersPostfix_doesNotRefreshNetwork() = runTest {
        val alphaZip = "123DE"
        createViewModel()

        searchZip(alphaZip)

        assertTrue(chartState is UvChartUiState.NoData)
    }

    @Test
    fun onLocationChanged_ifZip_has5Digits_andSomeLetters_doesNotRefreshNetwork() = runTest {
        val longAlphaZip = "12345ABC"
        createViewModel()

        searchZip(longAlphaZip)

        assertTrue(chartState is UvChartUiState.NoData)
    }

    @Test
    fun onLocationChanged_ifNoNetworkBefore_thenNetworkEnabled_refreshesNetwork() = runTest {
        val validZip = "12345"
        createViewModel()

        fakeUvService.exception = IOException()
        searchZip(validZip)

        var forecast = hourlyForecastRepo.getForecast(validZip, getDate())
        assertTrue(forecast is DataResult.Error)

        fakeUvService.exception = null
        searchZip(validZip)

        forecast = hourlyForecastRepo.getForecast(validZip, getDate())
        assertTrue(forecast is DataResult.Success)
    }

    @Test
    fun forceTrackingRefresh_withNoPreviousTrackingInfo_triggersRepositoryUpdate() = runTest {
        createViewModel()

        updateTracking(0.0, 0.0)

        val tracking = userTrackingRepo.getUserTrackingFlow(getDate().toString()).first()!!
        assertEquals(0.0, tracking.sunburnProgress, delta)
        assertEquals(0.0, tracking.vitaminDProgress, delta)
    }

    @Test
    fun forceTrackingRefresh_withArguments_updatesRepositoryValues() = runTest {
        createViewModel()

        updateTracking(1.0, 2.0)

        val date = getDate().toString()
        val tracking = userTrackingRepo.getUserTrackingFlow(date).first()!!
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
    fun onIsOnSnowOrWaterChanged_savesToRepo() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(true))

        assertTrue(userSettingsRepo.getIsOnSnowOrWaterFlow().first())
    }

    @Test
    fun onSkinTypeClicked_updatesSettings() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.SkinTypeClicked)

        assertEquals(AppDestination.SkinType, vm.settingsState.first())
    }

    @Test
    fun onClothingClicked_updatesSettings() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.ClothingClicked)

        assertEquals(AppDestination.Clothing, vm.settingsState.first())
    }

    @Test
    fun duringSearch_loading_isDisplayed() = runTest {
        createViewModel()

        val zip = FakeData.zip
        vm.onLocationBarEvent(LocationBarEvent.TextChanged(zip))
        vm.onLocationBarEvent(LocationBarEvent.LocationSearched(zip))

        assertEquals(ForecastState.Loading, vm.forecastState.first())

        triggerLocationUpdate()

        assertEquals(ForecastState.Done, vm.forecastState.first())
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
        fakeUvService.networkRequestCount = 0
        setLocation(FakeData.zip)
        createViewModel()

        triggerLocationUpdate()

        assertTrue(chartState is UvChartUiState.HasData)
        assertEquals(1, fakeUvService.networkRequestCount)
    }

    @Test
    fun init_ifRepoReportsIsOnboarded_isOnboarded() = runTest {

        createViewModel()

        assertEquals(AppState.Onboarded, vm.appState.first())
    }

    @Test
    @IsNotOnboarded
    fun init_ifRepoReportsNotOnboarded_isNotOnboarded() = runTest {

        createViewModel()

        assertEquals(AppState.NotOnboarded, vm.appState.first())
    }

    @Test
    @IsNotOnboarded
    fun init_ifRepoReportsLocationButNotOnboarded_isNotOnboarded() = runTest {
        setLocation(FakeData.zip)

        createViewModel()

        assertEquals(AppState.NotOnboarded, vm.appState.first())
    }

    @Test
    fun init_ifRepoReportsLocationAndOnboarded_isOnboarded() = runTest {
        setLocation(FakeData.zip)

        createViewModel()

        assertEquals(AppState.Onboarded, vm.appState.first())
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

    @Test
    @IsNotOnboarded
    fun init_whenNotOnboarded_doesNotLogAnalyticsEvent_forMainScreen() = runTest {
        createViewModel()

        triggerOnboardingUpdate()

        verify(exactly = 0) { mockAnalytics.viewScreen(AppDestination.Main.name) }
    }

    @Test
    fun init_whenOnboarded_logsAnalyticsEvent_forMainScreen() = runTest {
        createViewModel()

        triggerOnboardingUpdate()

        verify { mockAnalytics.viewScreen(AppDestination.Main.name) }
    }

    @Test
    fun onSearchLocation_ifInvalid_logsAnalyticsEvent() = runTest {
        createViewModel()

        searchZip("INVALID_ZIP_CODE")

        verify { mockAnalytics.searchLocation("INVALID_ZIP_CODE") }
    }

    @Test
    fun onSearchLocation_ifSuccessful_logsAnalyticsEvents() = runTest {
        createViewModel()

        searchZip(FakeData.zip)

        verify { mockAnalytics.searchLocation(FakeData.zip) }
        verify { mockAnalytics.searchSuccess(FakeData.zip) }
    }

    @Test
    fun onSearchLocation_ifError_logsAnalyticsEvent() = runTest {
        fakeUvService.exception = IOException("Network error")
        createViewModel()

        searchZip(FakeData.zip)

        verify { mockAnalytics.searchLocation(FakeData.zip) }
        verify { mockAnalytics.searchError(FakeData.zip, "Network error") }
    }

    @Test
    fun spf_whenChanged_logsAnalyticsEvent() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.SpfChanged("30"))

        verify { mockAnalytics.selectSpf(30) }
    }

    @Test
    fun spf_whenOnSnowOrWaterChanged_logsAnalyticsEvent() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.IsOnSnowOrWaterChanged(true))

        verify { mockAnalytics.selectReflectiveSurface(true) }
    }

    @Test
    fun tracking_logsAnalyticsEvents() = runTest {
        createViewModel()

        vm.onUvTrackingEvent(UvTrackingEvent.TrackingButtonClicked)
        vm.onUvTrackingEvent(UvTrackingEvent.TrackingButtonClicked)

        verify { mockAnalytics.startTracking() }
        verify { mockAnalytics.finishTracking() }
    }



    private suspend fun updateTracking(burnProgress: Double, vitaminDProgress: Double) {
        val date = getDate().toString()
        val userTracking = UserTracking(
            sunburnProgress = burnProgress,
            vitaminDProgress = vitaminDProgress
        )
        userTrackingRepo.setUserTracking(date, userTracking)
    }

    private fun getDate(): LocalDate {
        return LocalDate.now(clock)
    }

    private suspend fun setLocation(location: String) {
        userSettingsRepo.setLocation(location)
    }

    private suspend fun searchZip(zip: String) {
        vm.onLocationBarEvent(LocationBarEvent.TextChanged(zip))
        vm.onLocationBarEvent(LocationBarEvent.LocationSearched(zip))
        triggerLocationUpdate()
    }

    private suspend fun triggerLocationUpdate() {
        chartState = vm.uvChartUiState.first()
    }

    private suspend fun triggerOnboardingUpdate() {
        vm.appState.first()
    }
}