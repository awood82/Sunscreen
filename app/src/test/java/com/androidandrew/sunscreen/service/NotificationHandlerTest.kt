package com.androidandrew.sunscreen.service

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationHandlerTest {

    private val testChannelId = "Test Channel ID"
    private val mockManager = mockk<NotificationManager>(relaxed = true)
    private val mockBuilder = mockk<NotificationCompat.Builder>(relaxed = true)
    private val notificationHandler = DefaultNotificationHandler(testChannelId, mockManager, mockBuilder)

    @Test
    fun createChannel_delegates() {
        notificationHandler.createChannel(
            name = "channel name",
            importance = NotificationManager.IMPORTANCE_LOW
        )

        verify { mockManager.createNotificationChannel(any()) }
    }

    @Test
    fun deleteChannel_usesSameChannelId() {
        notificationHandler.deleteChannel()

        val channelIdSlot = slot<String>()
        verify { mockManager.deleteNotificationChannel(capture(channelIdSlot)) }
        assertEquals(testChannelId, channelIdSlot.captured)
    }

    @Test
    fun buildNotification_usesSameChannelId() {
        notificationHandler.buildNotification("title", "text")

        verify { mockBuilder.setChannelId(testChannelId) }
        verify { mockBuilder.setContentTitle("title") }
        verify { mockBuilder.setContentText("text") }
    }
}