package com.androidandrew.sunscreen.service

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.model.asModel
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sunscreen.tracksunexposure.SunTrackerSettings
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

    @Test
    fun bind_bindsToSunTrackerService() {
        bindWithFakeData()

//        val slot = slot<Intent>()
//        verify { context.bindService(capture(slot), any(), any()) }
//        assertEquals(SunTrackerService::class.qualifiedName, slot.captured.component?.className)

        verify { context.bindService(serviceIntent, any(), any()) }
    }

    @Test
    fun unbind_unbinds() {
        sunTrackerServiceController.unbind()

        verify { context.unbindService(any()) }
    }

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

    private fun bindWithFakeData() {
        val fakeData = SunTrackerSettings(
            uvPrediction = FakeEpaService.sampleDailyUvForecast.map {
                it.asModel()
            },
            hardcodedSkinType = 2,
            spf = 1,
            isOnReflectiveSurface = false
        )
        sunTrackerServiceController.apply {
            setSettings(fakeData)
            bind()
        }
    }
}