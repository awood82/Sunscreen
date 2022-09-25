package com.androidandrew.sunscreen.tracker.sunburn

import com.androidandrew.sunscreen.tracker.uv.UvPrediction
import com.androidandrew.sunscreen.tracker.uv.getUvNow
import java.lang.Double.min
import java.time.LocalTime

/**
 * Computes maximum time in the sun based on these factors:
 * Skin type, sunscreen SPF, UV index, altitude, and water/snow reflection.
 * Reference: https://www.omnicalculator.com/other/sunscreen
 */
object SunburnCalculator {

    const val maxSunUnits = 100.0
    const val spfNoSunscreen = 1
    const val NO_BURN_EXPECTED = 60 * 24.0 // minutes in a day

    private const val minuteMagicNumber = 33.3333 // Factor to get calculations into minutes
    private val lastMinuteInDay = LocalTime.MIDNIGHT.minusMinutes(1).minusNanos(1)

    /**
     * Returns the maximum minutes of sun exposure a person could get before starting to burn.
     * This assumes a constant UV factor, so isn't a perfect estimate.
     */
    fun computeMaxTime(uvIndex: Double, sunUnitsSoFar: Double = 0.0, skinType: Int,
                       spf: Int = spfNoSunscreen, altitudeInKm: Int = 0, isOnSnowOrWater: Boolean = false): Double {
        val maxMinutes = minuteMagicNumber * getSkinBlockFactor(skinType) * spf /
                (uvIndex * getAltitudeFactor(altitudeInKm) * getReflectionFactor(isOnSnowOrWater))

        return min(maxMinutes * (maxSunUnits - sunUnitsSoFar) / maxSunUnits, NO_BURN_EXPECTED)
    }

    /**
     * Returns the maximum minutes of sun exposure a person could get before starting to burn.
     * This takes into account changing UV factors each hour, so is a more accurate estimate.
     */
    fun computeMaxTime(uvPrediction: UvPrediction, currentTime: LocalTime = LocalTime.now(),
                       sunUnitsSoFar: Double = 0.0, skinType: Int, spf: Int = spfNoSunscreen,
                       altitudeInKm: Int = 0, isOnSnowOrWater: Boolean = false): Double {
        var maxMinutes = 0L
        var sunUnitsRemaining = maxSunUnits - sunUnitsSoFar

        while (sunUnitsRemaining > 0.0) {
            val simulatedTime = currentTime.plusMinutes(maxMinutes)

            // Check for no burn likely today
            if (simulatedTime.isAfter(lastMinuteInDay) && maxMinutes > 0) {
                return NO_BURN_EXPECTED
            }

            val sunUnits = computeSunUnitsInOneMinute(
                uvIndex = uvPrediction.getUvNow(simulatedTime),
                skinType = skinType,
                spf = spf,
                altitudeInKm = altitudeInKm,
                isOnSnowOrWater = isOnSnowOrWater
            )
            sunUnitsRemaining -= sunUnits
            maxMinutes++
        }

        return maxMinutes.toDouble()
    }

    /**
     * Returns the % of maximum sun exposure experienced by a person in one minute.
     */
    fun computeSunUnitsInOneMinute(uvIndex: Double, skinType: Int, spf: Int = spfNoSunscreen,
                                   altitudeInKm: Int = 0, isOnSnowOrWater: Boolean = false): Double {
        val maxTime = computeMaxTime(
            uvIndex = uvIndex,
            sunUnitsSoFar = 0.0,
            skinType = skinType,
            spf = spf,
            altitudeInKm = altitudeInKm,
            isOnSnowOrWater = isOnSnowOrWater
        )
        return when(maxTime) {
            NO_BURN_EXPECTED -> 0.0
            else -> maxSunUnits / maxTime
        }
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