package com.androidandrew.sunscreen.service

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SunTrackerServiceControllerTest {

    private val context = mockk<Context>(relaxed = true)
    private val serviceIntent = mockk<Intent>(relaxed = true)
    private val sunTrackerServiceController = SunTrackerServiceController(context, serviceIntent)

    @Ignore("It works with startService, but with startForegroundService, it fails with error: java.lang.NoSuchMethodError: 'android.content.ComponentName android.content.Context.startForegroundService(android.content.Intent)'")
    @Test
    fun start_starts() {
        sunTrackerServiceController.start()

        verify { context.startForegroundService(serviceIntent) }
    }

    @Test
    fun stop_stops() {
        sunTrackerServiceController.stop()

        verify { context.stopService(serviceIntent) }
    }
}