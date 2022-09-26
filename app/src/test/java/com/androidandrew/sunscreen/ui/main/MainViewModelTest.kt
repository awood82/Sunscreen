package com.androidandrew.sunscreen.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sunscreen.network.FakeEpaService
import com.androidandrew.sunscreen.util.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.*

@RunWith(AndroidJUnit4::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vm: MainViewModel
    private val fakeUvService = FakeEpaService
    private val noon = Instant.parse("2022-09-25T12:00:00.00Z")
    private val clockDefaultNoon = Clock.fixed(noon, ZoneId.of("UTC"))

    private fun createViewModel(clock: Clock = clockDefaultNoon) {
        vm = MainViewModel(fakeUvService, clock)
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
        val clock6pm = Clock.offset(clockDefaultNoon, Duration.ofHours(6))
        createViewModel(clock6pm)

        val burnTimeString = vm.burnTimeString.getOrAwaitValue()
        assertEquals("No burn expected", burnTimeString)
    }
}