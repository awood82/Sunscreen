package com.androidandrew.sunscreen.di

import android.app.Application
import android.app.NotificationManager
import android.app.Service
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.network.EpaApi
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.service.*
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

val databaseModule = module {
    fun provideDatabase(application: Application): SunscreenDatabase {
        return Room.databaseBuilder(
            application,
            SunscreenDatabase::class.java,
            "sunscreen_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { provideDatabase(androidApplication()) }
}

val networkModule = module {
    single { EpaApi.service }
}

val repositoryModule = module {
    single { SunscreenRepository(get()) }
}

val serviceModule = module {
    // For Sun Exposure Tracking Service
    single { androidApplication().getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager }
//    factory { (channelId: String) -> NotificationCompat.Builder(androidContext().applicationContext, channelId) }
    factory { NotificationCompat.Builder(androidApplication()) }
    factory<INotificationHandler> { (channelId: String) -> DefaultNotificationHandler(androidApplication(), channelId, get(), get()) }
//    single { NotificationChannelHandler(get()) }
//    single { NotificationBuilder(get()) }
    factory { SunTrackerServiceController(androidApplication(), get()) }
    factory<ISunTracker> { SunTracker(get(), get()) }
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
