package com.androidandrew.sunscreen.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.network.asUvPrediction
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.tracker.uv.trim
import com.androidandrew.sunscreen.testing.MainDispatcherRule
import com.androidandrew.sunscreen.util.toDateString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
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
    private val repo = SunscreenRepository(dbWrapper.userTrackingDao, dbWrapper.userSettingsDao)
    private val sunTracker = SunTracker(repo, clock)

    private val settings = SunTrackerSettings(
        uvPrediction = FakeEpaService.forecast.asUvPrediction().trim(),
        hardcodedSkinType = 2,
        spf = 1,
        isOnReflectiveSurface = false
    )

    @After
    fun tearDown() {
        sunTracker.stopTracking()
    }

    @Test
    fun startTracking_withNoSettingsSet_allowsIt() {
        sunTracker.startTracking()
    }

    @Test
    fun setSettings_databaseStarts_withEmptyValues() = runTest {
        sunTracker.setSettings(settings)

        val info = repo.getUserTrackingInfo(clock.toDateString())
        assertNull(info)
    }

    @Test
    fun startTracking_withSettingsSet_updatesDatabase() = runTest {
        sunTracker.setSettings(settings)

        sunTracker.startTracking()
        doShortDelay()

        val info = repo.getUserTrackingInfo(clock.toDateString())
        assertNotNull(info)
        assertTrue(info!!.vitaminDProgress > 0.0)
        assertTrue(info.burnProgress > 0.0)
    }

    @Test
    fun stopTracking_stopsUpdatingDatabase() = runTest {
        sunTracker.setSettings(settings)
        sunTracker.startTracking()
        doShortDelay()

        sunTracker.stopTracking()
        val info = repo.getUserTrackingInfo(clock.toDateString())
        doShortDelay()

        val newInfo = repo.getUserTrackingInfo(clock.toDateString())
        assertEquals(info, newInfo)
    }

    @Test
    fun setSettings_afterTrackingStarted_usesNewSettings() = runTest {
        sunTracker.setSettings(settings)
        sunTracker.startTracking()
        doShortDelay()

        sunTracker.setSettings(
            settings.copy(uvPrediction = emptyList())
        )
        val info = repo.getUserTrackingInfo(clock.toDateString())
        doShortDelay()

        val newInfo = repo.getUserTrackingInfo(clock.toDateString())
        assertEquals(info, newInfo)
    }
    
    private fun doShortDelay() {
        runBlocking { delay(1_100) }
    }
}