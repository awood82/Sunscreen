package com.androidandrew.sunscreen

import android.app.Application
import com.androidandrew.sunscreen.data.di.repositoryModule
import com.androidandrew.sunscreen.database.di.databaseModule
import com.androidandrew.sunscreen.di.*
import com.androidandrew.sunscreen.domain.di.domainModule
import com.androidandrew.sunscreen.network.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Koin Android logger
            androidLogger()
            //inject Android context
            androidContext(this@MainApplication)
            // use modules
            modules(domainModule, databaseModule, networkModule, repositoryModule, serviceModule, viewModelModule, appModule)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                // Override [log] to modify the tag and add a "global tag" prefix to it.
                // You can rename the String "global_tag_" as you see fit.
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, "$tag", message, t)
                }

                // Override [createStackElementTag] to include a add a "method name" to the tag.
                override fun createStackElementTag(element: StackTraceElement): String {
                    return String.format(
                        "%s:%s",
                        element.methodName,
                        super.createStackElementTag(element)
                    )
                }
            })
        }
    }
}