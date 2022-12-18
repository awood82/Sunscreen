package com.androidandrew.sunscreen.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.androidandrew.sunscreen.MainActivity
import com.androidandrew.sunscreen.service.DefaultNotificationHandler
import com.androidandrew.sunscreen.service.INotificationHandler
import com.androidandrew.sunscreen.tracksunexposure.SunTracker
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.ui.main.MainViewModel
import com.androidandrew.sunscreen.ui.chart.UvChartFormatter
import com.androidandrew.sunscreen.ui.init.InitViewModel
import com.androidandrew.sunscreen.ui.location.LocationViewModel
import com.androidandrew.sunscreen.util.LocationUtil
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

val serviceModule = module {
    single { androidApplication().getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager }
    factory { NotificationCompat.Builder(androidApplication()) }
    factory {
        val intentForNotification = Intent(androidApplication(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        PendingIntent.getActivity(androidApplication(), 0, intentForNotification, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    factory<INotificationHandler> { (channelId: String) -> DefaultNotificationHandler(channelId, get(), get(), get()) }
    factory { SunTrackerServiceController(androidApplication(), get()) }
    factory { SunTracker(get(), get()) }
}

val viewModelModule = module {
    viewModel { InitViewModel(get(), get()) }
    viewModel { LocationViewModel(get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get()) }
}

val appModule = module {
    single<Clock> { Clock.systemDefaultZone() }

    factory { LocationUtil() }
    factory { UvChartFormatter(androidContext()) }
}
