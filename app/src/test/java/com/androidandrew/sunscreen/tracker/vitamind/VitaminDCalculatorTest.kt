package com.androidandrew.sunscreen.tracker.vitamind

import com.androidandrew.sunscreen.tracker.sunburn.SunburnCalculator
import org.junit.Assert.*
import org.junit.Test

class VitaminDCalculatorTest {

    private val delta = 0.1

    @Test
    fun computeIUVitaminDInOneMinute_whenUvIndex_isZero_returnsZero() {
        val vitaminD = VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 0.0,
            skinType = 1
        )

        assertEquals(0.0, vitaminD, delta)
    }

    @Test
    fun computeIUVitaminDInOneMinute_whenUvIndex_isOne_returns20PercentOfMaxUnits() {
        val sunUnits = SunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = 1.0,
            skinType = 2
        )
        val vitaminD = VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 1.0,
            skinType = 2
        )

        assertEquals(0.2 * SunburnCalculator.maxSunUnits / sunUnits, VitaminDCalculator.recommendedIU / vitaminD, delta)
    }

    @Test
    fun computeIUVitaminDInOneMinute_whenUvIndex_isNine_returns10PercentOfMaxUnits() {
        val sunUnits = SunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = 9.0,
            skinType = 2
        )
        val vitaminD = VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 9.0,
            skinType = 2
        )

        assertEquals(0.1 * SunburnCalculator.maxSunUnits / sunUnits, VitaminDCalculator.recommendedIU / vitaminD, delta)
    }
}