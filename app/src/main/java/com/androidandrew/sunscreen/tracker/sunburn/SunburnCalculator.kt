package com.androidandrew.sunscreen.tracker.sunburn

import com.androidandrew.sunscreen.tracker.uv.UvPrediction
import java.time.LocalDateTime

/**
 * Computes maximum time in the sun based on these factors:
 * Skin type, sunscreen SPF, UV index, altitude, and water/snow reflection.
 * Reference: https://www.omnicalculator.com/other/sunscreen
 */
class SunburnCalculator {

    private val minuteMagicNumber = 33.3333 // Factor to get calculations into minutes

    /**
     * Returns the maximum minutes of sun exposure a person could get before starting to burn.
     * This assumes a constant UV factor, so isn't a perfect estimate.
     */
    fun computeMaxTime(skinType: Int, uvIndex: Double, spf: Int, altitudeInKm: Int, isOnSnowOrWater: Boolean): Double {
        return minuteMagicNumber * getSkinBlockFactor(skinType) * spf /
                (uvIndex * getAltitudeFactor(altitudeInKm) * getReflectionFactor(isOnSnowOrWater))
    }

    /**
     * Returns the maximum minutes of sun exposure a person could get before starting to burn.
     * This takes into account changing UV factors each hour, so is a more accurate estimate.
     */
//    fun computeMaxTime(skinType: Int, uvPrediction: UvPrediction, currentTime: LocalDateTime, spf: Int, altitudeInKm: Int, isOnSnowOrWater: Boolean): Double {
//        return minuteMagicNumber * getSkinBlockFactor(skinType) * spf /
//                (uvIndex * getAltitudeFactor(altitudeInKm) * getReflectionFactor(isOnSnowOrWater))
//    }

    /**
     * Returns the % of maximum sun exposure experienced by a person in one minute.
     */
    fun computeSunUnitsInOneMinute(skinType: Int, uvIndex: Double, spf: Int, altitudeInKm: Int, isOnSnowOrWater: Boolean): Double {
        return 100.0 / computeMaxTime(skinType, uvIndex, spf, altitudeInKm, isOnSnowOrWater)
    }

    private fun getSkinBlockFactor(type: Int): Int {
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