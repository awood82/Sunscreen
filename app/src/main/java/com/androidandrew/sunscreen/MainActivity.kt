package com.androidandrew.sunscreen

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.androidandrew.sunscreen.ui.SunscreenApp
import com.androidandrew.sunscreen.ui.theme.SunscreenTheme
import timber.log.Timber

@ExperimentalMaterial3WindowSizeClassApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate!
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        Timber.d("Loading SunscreenApp")
        setContent {
            val configuration = LocalConfiguration.current
            val useWideLayout = configuration.orientation != Configuration.ORIENTATION_PORTRAIT

            SunscreenTheme {
                SunscreenApp(useWideLayout = useWideLayout)
            }
        }
    }
}