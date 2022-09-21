package com.androidandrew.sunscreen.tracker.sunburn

/**
 * Computes maximum time in the sun based on these factors:
 * Skin type, sunscreen SPF, UV index, altitude, and water/snow reflection.
 * Reference: https://www.omnicalculator.com/other/sunscreen
 */
class SunburnCalculator {

    private val minuteMagicNumber = 33.3333 // Factor to get calculations into minutes

    fun computeMaxTime(skinType: Int, uvIndex: Double, spf: Int, altitudeInKm: Int, isOnSnowOrWater: Boolean): Double {
        return minuteMagicNumber * getSkinBlockFactor(skinType) * spf /
                (uvIndex * getAltitudeFactor(altitudeInKm) * getReflectionFactor(isOnSnowOrWater))
    }

    private fun getSkinBlockFactor(type: Int): Int {
        return when(type) {
            1 -> 2
            2 -> 3
            3 -> 6
            4 -> 9
            5 -> 12
            else -> 15
        }
    }

    private fun getReflectionFactor(isReflective: Boolean): Int {
        return when(isReflective) {
            false -> 1
            true -> 2
        }
    }

    private fun getAltitudeFactor(altitudeInKm: Int): Double {
        return 1.0 + 0.15 * (altitudeInKm / 1000.0)
    }
}