package com.androidandrew.sunscreen.tracksunexposure

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.model.FakeUvPredictions
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.HourlyForecastRepositoryImpl
import com.androidandrew.sunscreen.data.repository.UserSettingsRepositoryImpl
import com.androidandrew.sunscreen.data.repository.UserTrackingRepositoryImpl
import com.androidandrew.sunscreen.domain.usecases.GetLocalForecastForTodayUseCase
import com.androidandrew.sunscreen.model.UserTracking
import com.androidandrew.sunscreen.model.defaultUserClothing
import com.androidandrew.sunscreen.model.trim
import com.androidandrew.sunscreen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SunTrackerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = FakeData.clockDefaultNoon
    private val dbWrapper = FakeDatabaseWrapper()
    private val network = FakeEpaService
    private val userTrackingRepo = UserTrackingRepositoryImpl(dbWrapper.userTrackingDao)
    private val userSettingsRepo = UserSettingsRepositoryImpl(dbWrapper.userSettingsDao)
    private val hourlyForecastRepo = HourlyForecastRepositoryImpl(dbWrapper.hourlyForecastDao, network)
    private val getLocalForecastForToday = GetLocalForecastForTodayUseCase(
        userSettingsRepository = userSettingsRepo,
        hourlyForecastRepository = hourlyForecastRepo,
        clock = clock
    )
    private lateinit var sunTracker: SunTracker

    private val settings = SunTrackerSettings(
        uvPrediction = FakeUvPredictions.forecast.trim(),
        skinType = 1,
        clothing = defaultUserClothing,
        spf = 1,
        isOnReflectiveSurface = true
    )

    @Before
    fun setup() {
        runBlocking {
            dbWrapper.clearDatabase()
        }
        sunTracker = SunTracker(getLocalForecastForToday, userSettingsRepo, userTrackingRepo, clock, mainDispatcherRule.testDispatcher)
    }

    @After
    fun tearDown() {
        sunTracker.stopTracking()
        runBlocking {
            dbWrapper.clearDatabase()
        }
    }

    @Test
    fun startTracking_withNoSettingsSet_allowsIt() {
        sunTracker.startTracking()
    }

    @Test
    fun setSettings_databaseStarts_withEmptyValues() = runTest {
        setSettings(settings)

        val actualTracking = userTrackingRepo.getUserTracking(clock.toDateString())
        val expectedTracking = UserTracking(0.0, 0.0)
        assertEquals(expectedTracking, actualTracking)
    }

    @Test
    fun startTracking_withSettingsSet_updatesDatabase() = runTest {
        setSettings(settings)

        sunTracker.startTracking()
        doShortDelay()

        val info = userTrackingRepo.getUserTracking(clock.toDateString())
        assertNotNull(info)
        assertTrue(info.vitaminDProgress > 0.0)
        assertTrue(info.sunburnProgress > 0.0)
    }

    @Test
    fun stopTracking_stopsUpdatingDatabase() = runTest {
        setSettings(settings)
        sunTracker.startTracking()
        doShortDelay()

        sunTracker.stopTracking()
        val info = userTrackingRepo.getUserTracking(clock.toDateString())
        doShortDelay()

        val newInfo = userTrackingRepo.getUserTracking(clock.toDateString())
        assertEquals(info, newInfo)
    }

    @Test
    fun setSettings_afterTrackingStarted_usesNewSettings() = runTest {
        setSettings(settings)
        sunTracker.startTracking()
        val initialInfo = userTrackingRepo.getUserTracking(clock.toDateString())
        doShortDelay()

        setSettings(
            settings.copy(spf = 50, isOnReflectiveSurface = false)
        )
        doShortDelay()
        val info = userTrackingRepo.getUserTracking(clock.toDateString())
        doShortDelay()

        val newInfo = userTrackingRepo.getUserTracking(clock.toDateString())
        assertNotEquals(initialInfo, info)
        assertTrue(info.vitaminDProgress > 5.0) // Lots of exposure
        assertTrue(newInfo.vitaminDProgress - info.vitaminDProgress < 1.0) // New settings, much less exposure
        assertTrue(newInfo.vitaminDProgress > 0.0)
        assertTrue(newInfo.sunburnProgress > 0.0)
    }

    private fun setSettings(settings: SunTrackerSettings) {
        runBlocking {
            userSettingsRepo.setLocation(FakeData.zip)
            hourlyForecastRepo.setForecast(FakeEpaService.forecast)
            userSettingsRepo.setSkinType(settings.skinType)
            userSettingsRepo.setSpf(settings.spf)
            userSettingsRepo.setIsOnSnowOrWater(settings.isOnReflectiveSurface)
        }
    }
    
    private fun doShortDelay() {
        runBlocking { delay(1_100) }
    }
}