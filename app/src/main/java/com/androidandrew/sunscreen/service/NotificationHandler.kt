package com.androidandrew.sunscreen.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

abstract class INotificationHandler(val id: String) {
    abstract fun createChannel(name: String = id, importance: Int = NotificationManager.IMPORTANCE_DEFAULT)
    abstract fun deleteChannel()
    abstract fun buildNotification(title: String, text: String, @DrawableRes smallIcon: Int): Notification
    abstract fun createNotificationClickResponse(): PendingIntent?
}

class DefaultNotificationHandler(
    private val channelId: String,
    private val notificationManager: NotificationManager,
    private val notificationBuilder: NotificationCompat.Builder,
    private val notificationClickAction: PendingIntent
) : INotificationHandler(id = channelId) {
    override fun createChannel(name: String, importance: Int) {
        val channel = NotificationChannel(channelId, name, importance)
        notificationManager.createNotificationChannel(channel)
    }

    override fun deleteChannel() {
        notificationManager.deleteNotificationChannel(channelId)
    }

    override fun buildNotification(title: String, text: String, @DrawableRes smallIcon: Int): Notification {
        return notificationBuilder.apply {
            setChannelId(channelId)
            setContentTitle(title)
            setContentText(text)
            setContentIntent(createNotificationClickResponse())
            setSmallIcon(smallIcon)
        }.build()
    }

    override fun createNotificationClickResponse(): PendingIntent? {
        return notificationClickAction
    }
}