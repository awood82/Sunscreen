package com.androidandrew.sunscreen.service

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.database.FakeDatabaseWrapper
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.network.asUvPrediction
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.tracker.uv.trim
import com.androidandrew.sunscreen.util.MainCoroutineRule
import com.androidandrew.sunscreen.util.toDateString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SunTrackerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val clock = FakeData.clockDefaultNoon
    private val dbWrapper = FakeDatabaseWrapper()
    private val repo = SunscreenRepository(dbWrapper.db)
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

    @Test(expected = IllegalStateException::class)
    fun startTracking_withNoSettingsSet_throwsIllegalStateException() {
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
        runBlocking { delay(5000) }

        val info = repo.getUserTrackingInfo(clock.toDateString())
        assertNotNull(info)
        assertTrue(info!!.vitaminDProgress > 0.0)
        assertTrue(info.burnProgress > 0.0)
    }

    @Test
    fun stopTracking_stopsUpdatingDatabase() = runTest {
        sunTracker.setSettings(settings)
        sunTracker.startTracking()
        runBlocking { delay(5000) }

        sunTracker.stopTracking()
        val info = repo.getUserTrackingInfo(clock.toDateString())
        runBlocking { delay(5000) }
        val newInfo = repo.getUserTrackingInfo(clock.toDateString())

        assertEquals(info, newInfo)
    }
}