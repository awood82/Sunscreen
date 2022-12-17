package com.androidandrew.sunscreen.uvcalculators.vitamind

import com.androidandrew.sunscreen.uvcalculators.UvFactor
import com.androidandrew.sunscreen.uvcalculators.sunburn.SunburnCalculator
import org.junit.Assert.*
import org.junit.Test

class VitaminDCalculatorTest {

    private val delta = 0.1

    @Test
    fun computeIUVitaminDInOneMinute_whenUvIndex_isZero_returnsZero() {
        val vitaminD = VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 0.0,
            skinType = 1,
            clothing = UvFactor.Clothing.NAKED
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
            skinType = 2,
            clothing = UvFactor.Clothing.NAKED
        )

        assertEquals(0.2 * SunburnCalculator.maxSunUnits / sunUnits, VitaminDCalculator.recommendedIU / vitaminD, delta)
    }

    @Test
    fun computeIUVitaminDInOneMinute_whenUvIndex_isNine_returns10PercentOfMaxUnits() {
        val sunUnits = SunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = 9.0,
            skinType = 2,
        )
        val vitaminD = VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 9.0,
            skinType = 2,
            clothing = UvFactor.Clothing.NAKED
        )

        assertEquals(0.1 * SunburnCalculator.maxSunUnits / sunUnits, VitaminDCalculator.recommendedIU / vitaminD, delta)
    }

    /**
     * https://www.gbhealthwatch.com/Did-you-know-Get-VitD-Sun-Exposure.php - Table 1
     */
    @Test
    fun computeIUVitaminDInOneMinute_isInRange_forOvercomingMSWebsite() {
        assertInRange(VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 1, clothing = UvFactor.Clothing.SHORTS_T_SHIRT
        ), 10, 15)
        assertInRange(VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 2, clothing = UvFactor.Clothing.SHORTS_T_SHIRT
        ), 15, 20)
        assertInRange(VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 3, clothing = UvFactor.Clothing.SHORTS_T_SHIRT
        ), 20, 30)
        assertInRange(VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 4, clothing = UvFactor.Clothing.SHORTS_T_SHIRT
        ), 30, 40)
        assertInRange(VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 5, clothing = UvFactor.Clothing.SHORTS_T_SHIRT
        ), 40, 60)
    }

    private fun assertInRange(actualVitaminD: Double, lowerMinutes: Int, upperMinutes: Int, deltaMinutes: Double = 2.5) {
        val lowerBound = VitaminDCalculator.recommendedIU / (upperMinutes + deltaMinutes)
        assertTrue("Expected >= $lowerBound but was $actualVitaminD", actualVitaminD >= lowerBound)
        val upperBound = VitaminDCalculator.recommendedIU / (lowerMinutes - deltaMinutes)
        assertTrue("Expected <= $upperBound but was $actualVitaminD", actualVitaminD <= upperBound)
    }
}