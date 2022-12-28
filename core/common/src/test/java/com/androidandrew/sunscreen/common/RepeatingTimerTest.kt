package com.androidandrew.sunscreen.common

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.androidandrew.sunscreen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class RepeatingTimerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainDispatcherRule()

    private var count = 0

    private val repeatingTimer = RepeatingTimer(object : TimerTask() {
        override fun run() {
            count++
        }
    }, delayMillis = TimeUnit.SECONDS.toMillis(1), periodMillis = TimeUnit.SECONDS.toMillis(2))

    @Test
    fun repeatingTimer_start_ifAlreadyStarted_isNoOp() = runTest {
        repeatingTimer.start()
        repeatingTimer.start()
    }

    @Ignore("TODO: Test does not work. I think it uses Threads instead of Coroutines.")
    @Test
    fun repeatingTimer_executesAsOftenAsExpected() = runTest {
        // Expected: count increments at seconds 1, 3, 5, etc.
        repeatingTimer.start()

        delay(500)
        advanceUntilIdle() // Total elapsed time: 500 ms
        assertEquals(0, count)

        delay(500)
        advanceUntilIdle() // Total elapsed time: 1000 ms
        assertEquals(1, count)

        delay(1000)
        advanceUntilIdle() // Total elapsed time: 2000 ms
        assertEquals(1, count)

        delay(1000)
        advanceUntilIdle() // Total elapsed time: 3000 ms
        assertEquals(2, count)

        delay(2000)
        advanceUntilIdle() // Total elapsed time: 5000 ms
        assertEquals(3, count)

        repeatingTimer.cancel()
    }
}