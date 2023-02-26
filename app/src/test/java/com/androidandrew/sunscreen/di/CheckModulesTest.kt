package com.androidandrew.sunscreen.di

import android.app.Application
import android.app.NotificationManager
import android.app.Service
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.analytics.EventLogger
import com.androidandrew.sunscreen.data.di.repositoryModule
import com.androidandrew.sunscreen.database.AppDatabase
import com.androidandrew.sunscreen.database.HourlyForecastDao
import com.androidandrew.sunscreen.database.UserSettingsDao
import com.androidandrew.sunscreen.database.UserTrackingDao
import com.androidandrew.sunscreen.domain.di.domainModule
import com.androidandrew.sunscreen.network.di.networkModule
import com.androidandrew.sunscreen.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.mock.MockProviderRule

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class CheckModulesTest : KoinTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Declare Mock
    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz)
    }

    private val mockDatabaseModule = module {
        single { mockk<AppDatabase>(relaxed = true) }
        single { mockk<UserSettingsDao>(relaxed = true) }
        single { mockk<UserTrackingDao>(relaxed = true) }
        single { mockk<HourlyForecastDao>(relaxed = true) }
    }

    private val mockAnalyticsModule = module {
        single { mockk<EventLogger>(relaxed = true) }
    }

    private val context = mockk<Application>(relaxed = true)

    @Before
    fun setup() {
//        every { context.applicationContext } returns context
        every { context.getSystemService(Service.NOTIFICATION_SERVICE) } returns mockk<NotificationManager>(relaxed = true)
    }

    // verify the Koin configuration
    @Test
    fun checkAllModules() = checkModules {
        androidContext(context)
        modules(domainModule, mockDatabaseModule, networkModule, repositoryModule, serviceModule, mockAnalyticsModule, viewModelModule, appModule)
    }

    @Test
    fun verifyKoinApp() = runTest {
        koinApplication {
            androidContext(context)
            modules(domainModule, mockDatabaseModule, networkModule, repositoryModule, serviceModule, mockAnalyticsModule, viewModelModule, appModule)
            checkModules()
        }
    }
}