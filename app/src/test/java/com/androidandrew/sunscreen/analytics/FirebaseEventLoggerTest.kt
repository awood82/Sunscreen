package com.androidandrew.sunscreen.analytics

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FirebaseEventLoggerTest {

    private lateinit var logger: FirebaseEventLogger
    private val analytics: FirebaseAnalytics = mockk(relaxed = true)
    private val defaultStartTime = 1000000L

    @Before
    fun setup() {
        logger = FirebaseEventLogger(analytics)
    }

    @Test
    fun finishTracking_ifStartValuesAreKnown_logsThem() {
        val elapsedTimeInMillis = TimeUnit.MINUTES.toMillis(5)
        val elapsedPercent = 0.2f
        logger.startTracking(
            currentTimeInMillis = defaultStartTime,
            currentSunburnPercent0to1 = 0.1f,
            currentVitaminDPercent0to1 = 0.1f
        )

        logger.finishTracking(
            currentTimeInMillis = defaultStartTime + elapsedTimeInMillis,
            currentSunburnPercent0to1 = 0.1f + elapsedPercent,
            currentVitaminDPercent0to1 = 0.1f + elapsedPercent
        )

        val bundle = slot<Bundle>()
        verify { analytics.logEvent(Event.TRACKING_FINISH.name, capture(bundle)) }
        assertEquals(5, bundle.captured.getLong(Param.ELAPSED_MINUTES.name))
        assertEquals(20, bundle.captured.getInt(Param.ELAPSED_SUNBURN.name))
        assertEquals(20, bundle.captured.getInt(Param.ELAPSED_VITAMIN_D.name))
        assertEquals(30, bundle.captured.getInt(Param.TOTAL_SUNBURN.name))
        assertEquals(30, bundle.captured.getInt(Param.TOTAL_VITAMIN_D.name))
    }

    @Test
    fun finishTracking_ifStartValuesAreUnknown_logsNegativeOneForElapsed() {
        val elapsedTimeInMillis = TimeUnit.MINUTES.toMillis(5)
        val elapsedPercent = 0.2f
        logger.startTracking(
            currentTimeInMillis = defaultStartTime,
            currentSunburnPercent0to1 = 0.1f,
            currentVitaminDPercent0to1 = 0.1f
        )

        logger.forgetEverythingForTest()

        logger.finishTracking(
            currentTimeInMillis = defaultStartTime + elapsedTimeInMillis,
            currentSunburnPercent0to1 = 0.1f + elapsedPercent,
            currentVitaminDPercent0to1 = 0.1f + elapsedPercent
        )

        val bundle = slot<Bundle>()
        verify { analytics.logEvent(Event.TRACKING_FINISH.name, capture(bundle)) }
        assertEquals(-1, bundle.captured.getLong(Param.ELAPSED_MINUTES.name))
        assertEquals(-1, bundle.captured.getInt(Param.ELAPSED_SUNBURN.name))
        assertEquals(-1, bundle.captured.getInt(Param.ELAPSED_VITAMIN_D.name))
        assertEquals(30, bundle.captured.getInt(Param.TOTAL_SUNBURN.name))
        assertEquals(30, bundle.captured.getInt(Param.TOTAL_VITAMIN_D.name))
    }
}