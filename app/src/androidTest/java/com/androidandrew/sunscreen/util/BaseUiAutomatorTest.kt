package com.androidandrew.sunscreen.util

import android.content.Context
import android.graphics.Point
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.withFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.androidandrew.sunscreen.ui.main.MainFragment
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class BaseUiAutomatorTest : BaseUiTest() {
    lateinit var fragmentScenario: FragmentScenario<MainFragment>
    lateinit var uiDevice: UiDevice

    @Before
    override fun setup() {
        // NOTE on fixing pinned emulator: adb shell am task lock stop
        super.setup()
        fragmentScenario = launchFragmentUnderTest()
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @After
    fun teardown() {
        try {
            fragmentScenario.withFragment { requireActivity().stopLockTask() }
        } catch (e: Exception) {
        }
    }

    // Returns an x,y coordinate relative to a percentage of the screen's
    // dimensions.
    protected fun getClickPosition(xPercent: Float, yPercent: Float): Point {
        val width = uiDevice.displayWidth
        val height = uiDevice.displayHeight

        val x = xPercent * width / 100
        val y = yPercent * height / 100

//        Timber.d("click $x, $y")
        return Point(x.toInt(), y.toInt())
    }

    protected fun launchApp(packageName: String, timeoutMs: Long = 10000) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        context.startActivity(intent)

        // Wait for the app to appear
        uiDevice.wait(
            Until.hasObject(
            By.pkg(packageName).depth(0)), timeoutMs)
    }
}