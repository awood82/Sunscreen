@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.androidandrew.sunscreen.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.core.app.NotificationCompat
import com.androidandrew.sunscreen.MainActivity
import com.androidandrew.sunscreen.analytics.EventLogger
import com.androidandrew.sunscreen.analytics.FirebaseEventLogger
import com.androidandrew.sunscreen.data.di.repositoryModule
import com.androidandrew.sunscreen.database.di.databaseModule
import com.androidandrew.sunscreen.domain.di.domainModule
import com.androidandrew.sunscreen.network.di.networkModule
import com.androidandrew.sunscreen.service.DefaultNotificationHandler
import com.androidandrew.sunscreen.service.INotificationHandler
import com.androidandrew.sunscreen.service.SunTrackerService
import com.androidandrew.sunscreen.tracksunexposure.SunTracker
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.ui.main.MainViewModel
import com.androidandrew.sunscreen.ui.chart.UvChartFormatter
import com.androidandrew.sunscreen.ui.clothing.ClothingViewModel
import com.androidandrew.sunscreen.ui.location.LocationViewModel
import com.androidandrew.sunscreen.ui.skintype.SkinTypeViewModel
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
        val resumeAppWhenClicked = Intent(androidApplication(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        PendingIntent.getActivity(androidApplication(), 0, resumeAppWhenClicked, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    factory<INotificationHandler> { (channelId: String) -> DefaultNotificationHandler(channelId, get(), get(), get()) }
    factory { Intent(androidApplication(), SunTrackerService::class.java) }
    factory { SunTrackerServiceController(androidApplication(), get()) }
    factory { SunTracker(get(), get(), get(), get()) }
}

val analyticsModule = module {
    single<EventLogger> { FirebaseEventLogger() }
}

val viewModelModule = module {
    viewModel { LocationViewModel(get(), get(), get()) }
    viewModel { SkinTypeViewModel(get(), get()) }
    viewModel { ClothingViewModel(get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}

val appModule = module {
    single<Clock> { Clock.systemDefaultZone() }

    factory { LocationUtil() }
    factory { UvChartFormatter(androidContext()) }
}

val allModules = listOf(domainModule, databaseModule, networkModule, repositoryModule, serviceModule, analyticsModule, viewModelModule, appModule)
