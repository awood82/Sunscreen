package com.androidandrew.sunscreen.common

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.androidandrew.sunscreen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    private var repeatingTimer = RepeatingTimer(
        initialDelayMillis = TimeUnit.SECONDS.toMillis(1),
        repeatPeriodMillis = TimeUnit.SECONDS.toMillis(2),
        defaultDispatcher = mainCoroutineRule.testDispatcher,
        action = { count++ }
    )

    @Test
    fun repeatingTimer_start_ifAlreadyStarted_isNoOp() = runTest {
        repeatingTimer.start()
        repeatingTimer.start()

        repeatingTimer.cancel()
    }

    @Test
    fun repeatingTimer_start_afterCancel_isAllowed() = runTest {
        repeatingTimer.start()
        repeatingTimer.cancel()

        repeatingTimer.start()
        repeatingTimer.cancel()
    }

    @Test
    fun repeatingTimer_start_afterCreatingNewTimer_works() = runTest {
        repeatingTimer.start()
        repeatingTimer.cancel()

        repeatingTimer = RepeatingTimer(
            initialDelayMillis = TimeUnit.SECONDS.toMillis(1),
            repeatPeriodMillis = TimeUnit.SECONDS.toMillis(2),
            defaultDispatcher = mainCoroutineRule.testDispatcher,
            action = { count++ }
        )
        repeatingTimer.start()
        repeatingTimer.cancel()
    }

    @Test
    fun repeatingTimer_afterCancel_stopsExecuting() = runTest {
        repeatingTimer.start()

        delay(1000)
        delay(2000)
        delay(2000)
        assertEquals(3, count)

        repeatingTimer.cancel()

        delay(2_000)
        delay(2_000)
        assertEquals(3, count)
    }

    @Test
    fun repeatingTimer_executesAsOftenAsExpected() = runTest {
        // Expected: count increments at seconds 1, 3, 5, etc.
        repeatingTimer.start()

        delay(500) // Total elapsed time: 500 ms
        assertEquals(0, count)

        delay(500) // Total elapsed time: 1000 ms
        assertEquals(1, count)

        delay(1000) // Total elapsed time: 2000 ms
        assertEquals(1, count)

        delay(1000) // Total elapsed time: 3000 ms
        assertEquals(2, count)

        delay(2000) // Total elapsed time: 5000 ms
        assertEquals(3, count)

        repeatingTimer.cancel()
    }
}