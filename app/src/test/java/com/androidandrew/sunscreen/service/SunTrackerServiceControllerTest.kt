package com.androidandrew.sunscreen.service

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.network.asUvPredictionPoint
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SunTrackerServiceControllerTest {

    private val context = mockk<Context>(relaxed = true)
    private val clock = FakeData.clockDefaultNoon
    private val sunTrackerServiceController = SunTrackerServiceController(context, clock)

    @Test
    fun bind_bindsToSunTrackerService() {
        bindWithFakeData()

        val slot = slot<Intent>()
        verify { context.bindService(capture(slot), any(), any()) }
        assertEquals(SunTrackerService::class.qualifiedName, slot.captured.component?.className)
    }

    @Test
    fun unbind_unbinds() {
        sunTrackerServiceController.unbind()

        verify { context.unbindService(any()) }
    }

    @Test
    fun start_starts() {
        sunTrackerServiceController.start()

        verify { context.startForegroundService(any()) }
    }

    @Test
    fun stop_stops() {
        sunTrackerServiceController.stop()

        verify { context.stopService(any()) }
    }

    private fun bindWithFakeData() {
        val fakeData = SunTrackerSettings(
            uvPrediction = FakeEpaService.sampleDailyUvForecast.map {
                it.asUvPredictionPoint()
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