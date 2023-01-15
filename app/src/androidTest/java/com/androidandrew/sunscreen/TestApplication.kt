package com.androidandrew.sunscreen

import android.app.Application
import com.androidandrew.sunscreen.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Koin Android logger
            androidLogger()
            androidContext(this@TestApplication)
            // Order matters! Overriding modules go last.
            modules(allModules)
        }

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