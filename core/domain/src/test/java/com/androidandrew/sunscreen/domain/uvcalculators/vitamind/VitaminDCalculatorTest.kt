package com.androidandrew.sunscreen.domain.uvcalculators.vitamind

import com.androidandrew.sunscreen.domain.ConvertSpfUseCase
import com.androidandrew.sunscreen.domain.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.model.ClothingBottom
import com.androidandrew.sunscreen.model.ClothingTop
import com.androidandrew.sunscreen.model.UserClothing
import org.junit.Assert.*
import org.junit.Test

class VitaminDCalculatorTest {

    private val spfUseCase = ConvertSpfUseCase()
    private val vitaminDCalculator = VitaminDCalculator(spfUseCase)
    private val sunburnCalculator = SunburnCalculator(spfUseCase)
    private val delta = 0.25
    private val NAKED = UserClothing(top = ClothingTop.NOTHING, bottom = ClothingBottom.NOTHING)
    private val SHORTS_T_SHIRT = UserClothing(top = ClothingTop.T_SHIRT, bottom = ClothingBottom.SHORTS)

    @Test
    fun computeIUVitaminDInOneMinute_whenUvIndex_isZero_returnsZero() {
        val vitaminD = vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 0.0,
            skinType = 1,
            clothing = NAKED
        )

        assertEquals(0.0, vitaminD, delta)
    }

    @Test
    fun computeIUVitaminDInOneMinute_whenUvIndex_isOne_returns20PercentOfMaxUnits() {
        val sunUnits = sunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = 1.0,
            skinType = 2
        )
        val vitaminD = vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 1.0,
            skinType = 2,
            clothing = NAKED
        )

        assertEquals(0.2 * SunburnCalculator.MAX_SUN_UNITS / sunUnits, VitaminDCalculator.RECOMMENDED_IU / vitaminD, delta)
    }


    @Test
    fun computeIUVitaminDInOneMinute_whenSpfIsInvalidZero_itIsTreatedLikeValidOne() {
        val sunUnits = sunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = 1.0,
            skinType = 2,
            spf = 0
        )
        val vitaminD = vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 1.0,
            skinType = 2,
            clothing = NAKED,
            spf = 0
        )

        assertEquals(0.2 * SunburnCalculator.MAX_SUN_UNITS / sunUnits, VitaminDCalculator.RECOMMENDED_IU / vitaminD, delta)
    }

    @Test
    fun computeIUVitaminDInOneMinute_whenUvIndex_isNine_returns10PercentOfMaxUnits() {
        val sunUnits = sunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = 9.0,
            skinType = 2,
        )
        val vitaminD = vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 9.0,
            skinType = 2,
            clothing = NAKED
        )

        assertEquals(0.1 * SunburnCalculator.MAX_SUN_UNITS / sunUnits, VitaminDCalculator.RECOMMENDED_IU / vitaminD, delta)
    }

    /**
     * https://www.gbhealthwatch.com/Did-you-know-Get-VitD-Sun-Exposure.php - Table 1
     */
    @Test
    fun computeIUVitaminDInOneMinute_isInRange_forOvercomingMSWebsite() {
        assertInRange(
            vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 1, clothing = SHORTS_T_SHIRT
        ), 10, 15, deltaMinutes = 3.0)
        assertInRange(
            vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 2, clothing = SHORTS_T_SHIRT
        ), 15, 20, deltaMinutes = 4.5)
        assertInRange(
            vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 3, clothing = SHORTS_T_SHIRT
        ), 20, 30)
        assertInRange(
            vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 4, clothing = SHORTS_T_SHIRT
        ), 30, 40)
        assertInRange(
            vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = 3.0, skinType = 5, clothing = SHORTS_T_SHIRT
        ), 40, 60)
    }

    private fun assertInRange(actualVitaminD: Double, lowerMinutes: Int, upperMinutes: Int, deltaMinutes: Double = 2.5) {
        val lowerBound = VitaminDCalculator.RECOMMENDED_IU / (upperMinutes + deltaMinutes)
        assertTrue("Expected >= lower bound $lowerBound but was $actualVitaminD", actualVitaminD >= lowerBound)
        val upperBound = VitaminDCalculator.RECOMMENDED_IU / (lowerMinutes - deltaMinutes)
        assertTrue("Expected <= upper bound $upperBound but was $actualVitaminD", actualVitaminD <= upperBound)
    }
}