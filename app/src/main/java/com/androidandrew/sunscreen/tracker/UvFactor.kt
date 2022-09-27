package com.androidandrew.sunscreen.tracker

object UvFactor {

    fun getSkinBlockFactor(type: Int): Int {
        return when(type) {
            1 -> 2
            2 -> 3
            3 -> 6
            4 -> 9
            5 -> 12
            6 -> 15
            else -> 0
        }
    }

    fun getReflectionFactor(isReflective: Boolean): Int {
        return when(isReflective) {
            false -> 1
            true -> 2
        }
    }

    fun getAltitudeFactor(altitudeInKm: Int): Double {
        return 1.0 + 0.15 * (altitudeInKm / 1000.0)
    }
}