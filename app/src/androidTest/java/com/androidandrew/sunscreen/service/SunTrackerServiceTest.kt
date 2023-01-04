package com.androidandrew.sunscreen.service

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserTrackingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import java.time.LocalDate

@LargeTest
@RunWith(AndroidJUnit4::class)
class SunTrackerServiceTest {

    private val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    private val sunTrackerIntent = Intent(appContext, SunTrackerService::class.java)
    private val userTrackingRepo: UserTrackingRepository by inject(UserTrackingRepository::class.java)
    private val userSettingsRepo: UserSettingsRepository by inject(UserSettingsRepository::class.java)
    private val fakeDate = LocalDate.now(FakeData.clockDefaultNoon).toString()

    @After
    fun tearDown() {
        appContext.stopService(sunTrackerIntent)
    }

    @Test
    fun startService_startsTrackingSunExposure() {
        runBlocking {
            val info = userTrackingRepo.getUserTracking(fakeDate)
            assertEquals(0.0, info?.vitaminDProgress)
            assertEquals(0.0, info?.sunburnProgress)

            userSettingsRepo.setLocation(FakeData.zip)
        }

        appContext.startService(sunTrackerIntent)

        runBlocking {
            delay(2_000)
            val info = userTrackingRepo.getUserTracking(fakeDate)
            assertNotEquals(0.0, info?.vitaminDProgress)
            assertNotEquals(0.0, info?.sunburnProgress)
        }
    }
}