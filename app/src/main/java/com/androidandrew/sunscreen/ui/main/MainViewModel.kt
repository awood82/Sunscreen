package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidandrew.sunscreen.tracker.sunburn.MinuteTimer
import java.util.*

class MainViewModel : ViewModel() {

    private val _sunUnitsToday = MutableLiveData(0.0) // ~100.0 means almost-certain sunburn
    val sunUnitsToday: LiveData<Double> = _sunUnitsToday

    private val minuteTimer = MinuteTimer(object : TimerTask() {
        override fun run() {
            _sunUnitsToday.postValue( (_sunUnitsToday.value)?.plus(5.0) ) // TODO: hard-coded test value
        }
    })

    fun onStart() {
        minuteTimer.start()
    }

    fun onStop() {
        minuteTimer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        minuteTimer.cancel()
    }
}