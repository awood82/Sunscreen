package com.androidandrew.sunscreen.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.androidandrew.sunscreen.MainActivity

abstract class INotificationHandler(val id: String) {
    abstract fun createChannel(name: String = id, importance: Int = NotificationManager.IMPORTANCE_DEFAULT)
    abstract fun deleteChannel()
    abstract fun buildNotification(title: String, text: String, @DrawableRes smallIcon: Int): Notification
    abstract fun createNotificationClickResponse(): PendingIntent?
}

class DefaultNotificationHandler(
    private val context: Context,
    private val channelId: String,
    private val notificationManager: NotificationManager,
    private val notificationBuilder: NotificationCompat.Builder
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
        val notifyIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context, 0, notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}