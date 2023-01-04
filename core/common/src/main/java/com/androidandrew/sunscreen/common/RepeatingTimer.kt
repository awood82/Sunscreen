package com.androidandrew.sunscreen.common

import kotlinx.coroutines.*

class RepeatingTimer(private val initialDelayMillis: Long, private val repeatPeriodMillis: Long,
                     defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
                     private val action: () -> Unit) {

    private var isStarted = false
    private var job = SupervisorJob()
    private var coroutineScope = CoroutineScope(defaultDispatcher + job)

    fun start() {
        startIfNotStartedYet()
    }

    fun cancel() {
        coroutineScope.coroutineContext.cancelChildren()
        isStarted = false
    }

    private fun startIfNotStartedYet() {
        if (!isStarted) {
            isStarted = true

            coroutineScope.launch {
                delay(initialDelayMillis)
                while (true) {
                    action()
                    delay(repeatPeriodMillis)
                }
            }
        }
    }
}