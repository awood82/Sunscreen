package com.androidandrew.sunscreen.domain.uvcalculators.vitamind

import com.androidandrew.sunscreen.domain.ConvertSpfUseCase
import com.androidandrew.sunscreen.domain.UvFactor
import com.androidandrew.sunscreen.model.UserClothing
import kotlin.math.pow

class VitaminDCalculator(private val convertSpfUseCase: ConvertSpfUseCase) {

    companion object {
        val RECOMMENDED_IU = 1000.0 // Some studies recommend less (400), others more (4000)
    }

    private val minuteMagicNumber = 33.3333 // Factor to get calculations into minutes

    /**
     * Returns the expected Vitamin D produced by a person in one minute, measured in IU.
     * Uses a ratio estimated from time to sunburn in:
     * https://www.climate-policy-watcher.org/ultraviolet-radiation-2/calculation-of-optimal-times-for-exposure-to-sunlight.html
     */
    fun computeIUVitaminDInOneMinute(uvIndex: Double, skinType: Int, clothing: UserClothing,
        spf: Int = ConvertSpfUseCase.MIN_SPF, altitudeInKm: Int = 0): Double {
        if (uvIndex <= 0.0) {
            return 0.0
        }
        val maxMinutes = minuteMagicNumber * UvFactor.getSkinBlockFactor(skinType) * convertSpfUseCase.forCalculations(spf) /
                (uvIndex * UvFactor.getAltitudeFactor(altitudeInKm))
        val vitDConversionFactor = when {
            uvIndex > 0.5 && uvIndex <= 1 -> 0.2
            uvIndex > 1 && uvIndex < 9 -> 0.179 * uvIndex.pow(-0.269)
            uvIndex >= 9 -> 0.1
            else -> return 0.0
        }
        return RECOMMENDED_IU * UvFactor.getSkinExposedFactor(clothing) / 100.0 / (maxMinutes * vitDConversionFactor)
    }
}