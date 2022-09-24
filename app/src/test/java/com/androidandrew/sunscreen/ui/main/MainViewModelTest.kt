package com.androidandrew.sunscreen.ui.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.tracker.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.util.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class MainViewModelTest {

    private lateinit var vm: MainViewModel
    private var currentTimeDefaultNoon: LocalTime = LocalTime.NOON

    private fun createViewModel() {
        vm = MainViewModel(currentTimeDefaultNoon)
    }

    @Test
    fun burnTimeString_ifBurnExpected_isSet() {
        createViewModel()

        // Accept any "<number> min" string
        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertTrue(burnTimeString.endsWith("min"))
        val firstChar = burnTimeString[0]
        assertTrue(firstChar.isDigit())
    }

    @Test
    fun burnTimeString_ifNoBurnExpected_isNotSet() {
        currentTimeDefaultNoon = currentTimeDefaultNoon.plusHours(6)
        createViewModel()

        // Expects NO_BURN_EXPECTED as Int for now
        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertTrue(burnTimeString.startsWith(SunburnCalculator.NO_BURN_EXPECTED.toInt().toString()))
    }
}