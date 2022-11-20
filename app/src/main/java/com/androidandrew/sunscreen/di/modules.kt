package com.androidandrew.sunscreen.di

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.network.EpaApi
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.service.DefaultNotificationHandler
import com.androidandrew.sunscreen.service.INotificationHandler
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.ui.main.MainViewModel
import com.androidandrew.sunscreen.ui.chart.UvChartFormatter
import com.androidandrew.sunscreen.ui.init.InitViewModel
import com.androidandrew.sunscreen.ui.location.LocationViewModel
import com.androidandrew.sunscreen.util.LocationUtil
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

val appModule = module {
    fun provideDatabase(context: Context): SunscreenDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SunscreenDatabase::class.java,
            "sunscreen_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    single<Clock> { Clock.systemDefaultZone() }
    single { EpaApi.service }
    single { provideDatabase(androidContext()) }
    single { SunscreenRepository(get(), get()) }

    // For Sun Exposure Tracking Service
    single { androidContext().applicationContext.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager }
//    factory { (channelId: String) -> NotificationCompat.Builder(androidContext().applicationContext, channelId) }
    factory { NotificationCompat.Builder(androidContext().applicationContext) }
    factory<INotificationHandler> { (channelId: String) -> DefaultNotificationHandler(channelId, get(), get()) }
//    single { NotificationChannelHandler(get()) }
//    single { NotificationBuilder(get()) }
    factory { SunTrackerServiceController(androidContext().applicationContext, get()) }

    factory { LocationUtil() }
    factory { UvChartFormatter(androidContext()) }

    viewModel { InitViewModel(get(), get()) }
    viewModel { LocationViewModel(get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get()) }
}
