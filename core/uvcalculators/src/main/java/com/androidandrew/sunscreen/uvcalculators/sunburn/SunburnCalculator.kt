package com.androidandrew.sunscreen.uvcalculators.sunburn

import com.androidandrew.sunscreen.model.UvPrediction
import com.androidandrew.sunscreen.model.getUvNow
import com.androidandrew.sunscreen.uvcalculators.UvFactor
import java.lang.Double.min
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Computes maximum time in the sun based on these factors:
 * Skin type, sunscreen SPF, UV index, altitude, and water/snow reflection.
 * Reference: https://www.omnicalculator.com/other/sunscreen
 */
object SunburnCalculator {

    const val maxSunUnits = 100.0
    const val spfNoSunscreen = 1
    const val spfMaxSunscreen = 50
    val NO_BURN_EXPECTED = TimeUnit.DAYS.toMinutes(1).toDouble()

    private const val minuteMagicNumber = 33.3333 // Factor to get calculations into minutes
    private val lastMinuteInDay = LocalTime.MIDNIGHT.minusMinutes(1).minusNanos(1)

    /**
     * Returns the maximum minutes of sun exposure a person could get before starting to burn.
     * This assumes a constant UV factor, so isn't a perfect estimate.
     */
    fun computeMaxTime(uvIndex: Double, sunUnitsSoFar: Double = 0.0, skinType: Int,
                       spf: Int = spfNoSunscreen, altitudeInKm: Int = 0, isOnSnowOrWater: Boolean = false): Double {
        val maxMinutes = minuteMagicNumber * UvFactor.getSkinBlockFactor(skinType) * getSpfClamped(spf) /
                (uvIndex * UvFactor.getAltitudeFactor(altitudeInKm) * UvFactor.getReflectionFactor(
                    isOnSnowOrWater))

        return min(maxMinutes * (maxSunUnits - sunUnitsSoFar) / maxSunUnits, NO_BURN_EXPECTED)
    }

    /**
     * Returns the maximum minutes of sun exposure a person could get before starting to burn.
     * This takes into account changing UV factors each hour, so is a more accurate estimate.
     */
    fun computeMaxTime(uvPrediction: UvPrediction, currentTime: LocalTime = LocalTime.now(),
                       sunUnitsSoFar: Double = 0.0, skinType: Int, spf: Int = spfNoSunscreen,
                       altitudeInKm: Int = 0, isOnSnowOrWater: Boolean = false): Double {
        var sunUnitsRemaining = maxSunUnits - sunUnitsSoFar
        val minutesLeftInDay = Duration.between(currentTime, lastMinuteInDay)
        var maxMinutes = 0.0

        if (sunUnitsRemaining <= 0) {
            return 0.0
        }

        while (maxMinutes < minutesLeftInDay.toMinutes()) {
            val simulatedTime = currentTime.plusMinutes(maxMinutes.toLong())
            val sunUnits = computeSunUnitsInOneMinute(
                uvIndex = uvPrediction.getUvNow(simulatedTime),
                skinType = skinType,
                spf = getSpfClamped(spf),
                altitudeInKm = altitudeInKm,
                isOnSnowOrWater = isOnSnowOrWater
            )
            maxMinutes++
            sunUnitsRemaining -= sunUnits
            if (sunUnitsRemaining <= 0.0) {
                // Adding a negative number will subtract a fraction of a minute
                return maxMinutes + sunUnitsRemaining / sunUnits
            }
        }
        return NO_BURN_EXPECTED
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
            spf = getSpfClamped(spf),
            altitudeInKm = altitudeInKm,
            isOnSnowOrWater = isOnSnowOrWater
        )
        return when (maxTime) {
            NO_BURN_EXPECTED -> 0.0
            else -> maxSunUnits / maxTime
        }
    }

    fun getSpfClamped(spf: Int?): Int {
        return when {
            spf == null -> spfNoSunscreen
            spf > spfMaxSunscreen -> spfMaxSunscreen
            spf <= spfNoSunscreen -> spfNoSunscreen
            else -> spf
        }
    }
}