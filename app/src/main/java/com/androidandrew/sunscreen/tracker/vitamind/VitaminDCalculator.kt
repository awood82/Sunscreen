package com.androidandrew.sunscreen.tracker.vitamind

import com.androidandrew.sunscreen.tracker.UvFactor
import kotlin.math.pow

object VitaminDCalculator {

    const val recommendedIU = 1000.0 // Some studies recommend less (400), others more (4000)
    const val spfNoSunscreen = 1

    private const val minuteMagicNumber = 33.3333 // Factor to get calculations into minutes

    /**
     * Returns the expected Vitamin D produced by a person in one minute, measured in IU.
     * Uses a ratio estimated from time to sunburn in:
     * https://www.climate-policy-watcher.org/ultraviolet-radiation-2/calculation-of-optimal-times-for-exposure-to-sunlight.html
     */
    fun computeIUVitaminDInOneMinute(uvIndex: Double, skinType: Int, spf: Int = spfNoSunscreen,
                                   altitudeInKm: Int = 0, percentOfBodyExposed: Double = 100.0): Double {
        val maxMinutes = minuteMagicNumber * UvFactor.getSkinBlockFactor(skinType) * spf /
                (uvIndex * UvFactor.getAltitudeFactor(altitudeInKm))
        val vitDConversionFactor = when {
            uvIndex > 0.5 && uvIndex <= 1 -> 0.2
            uvIndex > 1 && uvIndex < 9 -> 0.179 * uvIndex.pow(-0.269)
            uvIndex >= 9 -> 0.1
            else -> return 0.0
        }
        return recommendedIU * percentOfBodyExposed / 100.0 / (maxMinutes * vitDConversionFactor)
    }
}