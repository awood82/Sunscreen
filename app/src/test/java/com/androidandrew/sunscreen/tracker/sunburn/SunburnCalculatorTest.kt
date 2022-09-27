package com.androidandrew.sunscreen.tracker.sunburn

import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class SunburnCalculatorTest {
    
    private val delta = 0.1
    private val maxTimeDelta = 0.99

    // A baseline test to compare to the omni calculator website
    @Test
    fun computeMaxTime_withSkinType4_andUV5_is60Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(60.0, maxTime, delta)
    }

    // Being on snow or in the water half the maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType4_andUV5_andReflectiveSurface_is30Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = true
        )

        assertEquals(30.0, maxTime, delta)
    }

    // Higher UV index reduces the maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType4_andUV10_is30Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 10.0,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(30.0, maxTime, delta)
    }

    // Wearing sunscreen w/ SPF increases the maximum sun exposure time linearly
    @Test
    fun computeMaxTime_withSkinType4_andUV5_andSPF10_is600Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 4,
            spf = 10,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(600.0, maxTime, delta)
    }

    // Having darker color skin increases maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType5_andUV5_is80Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 5,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(80.0, maxTime, delta)
    }

    // Having darker color skin increases maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType6_andUV5_is100Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 6,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(100.0, maxTime, delta)
    }

    // Having lighter color skin reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType3_andUV5_is30Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 3,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(40.0, maxTime, delta)
    }

    // Having lighter color skin reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType2_andUV5_is20Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 2,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(20.0, maxTime, delta)
    }

    // Having lighter color skin reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType1_andUV5_is13_3Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 1,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(13.33, maxTime, delta)
    }

    // Higher altitude reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType3_andUV5_at2km_is30Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 3,
            spf = 1,
            altitudeInKm = 2000,
            isOnSnowOrWater = false
        )

        assertEquals(30.7, maxTime, delta)
    }

    // Higher altitude reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType3_andUV5_at7km_is19Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 3,
            spf = 1,
            altitudeInKm = 7000,
            isOnSnowOrWater = false
        )

        assertEquals(19.5, maxTime, delta)
    }

    // If skintype isn't set, we can't compute
    @Test
    fun computeMaxTime_withSkinTypeUnknown_is0Minutes() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            skinType = 0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = true
        )

        assertEquals(0.0, maxTime, delta)
    }

    // If the max time is 20 minutes, then the sun units in one minute is 5. 100% / 20min = 5%/min
    @Test
    fun computeSunUnits_withMaxTime20Minutes_is5Percent() {
        val sunUnits = SunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = 5.0,
            skinType = 2,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(5.0, sunUnits, delta)
    }

    // maxTime is 30 minutes w/ constant UV, so it should be less w/ rising UV
    @Test
    fun computeMaxTime_withRisingUv_overFullHour_takesAverage() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 5.0) // 60 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 10.0) // 30 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(45.0, maxTime, maxTimeDelta)
    }

    // maxTime is 30 minutes w/ constant UV, so it should be more w/ falling UV
    @Test
    fun computeMaxTime_withFallingUv_isLessThanAverage_overFullHour() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        // average of 60 and 30 is 45.0
        assertTrue(maxTime < 45.0)
    }

    @Test
    fun computeMaxTime_withSunUnitsSoFar_usesThem() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 10.0) // 30 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            sunUnitsSoFar = 50.0,
            skinType = 4, // halfway, so burn time should be half of 30
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(15.0, maxTime, maxTimeDelta)
    }

    @Test
    fun computeMaxTime_betweenHalfHours() {
        val elevenAmPrediction = UvPredictionPoint(LocalTime.NOON.minusHours(1), 2.5) // 120 minutes to burn
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 3.0) // 100 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 2.5) // 120 minutes to burn
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)
        val currentTime = LocalTime.NOON.minusMinutes(30) // 11:30 am

        val maxTime = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = currentTime,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(110.0, maxTime, maxTimeDelta)
    }

    @Test
    fun computeMaxTime_withInsaneUv_sameForRisingToApex_asFallingFromApex() {
        val elevenAmPrediction = UvPredictionPoint(LocalTime.NOON.minusHours(1), 5.0) // 60 minutes to burn
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 150.0) // 2 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)

        val rising = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON.minusMinutes(3),
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        val falling = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON.plusMinutes(3),
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(rising, falling, delta)
    }

    @Test
    fun computeMaxTime_whenAlreadyBurned_isZero() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            sunUnitsSoFar = SunburnCalculator.maxSunUnits,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(0.0, maxTime, delta)
    }

    @Test
    fun computeMaxTime_whenCrispy_isZero() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            sunUnitsSoFar = SunburnCalculator.maxSunUnits * 2,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(0.0, maxTime, delta)
    }

    @Test
    fun computeMaxTime_whenAlmostBurned_isOneOrLess() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            sunUnitsSoFar = SunburnCalculator.maxSunUnits - 0.1,
            skinType = 4,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertTrue(maxTime > 0.0)
        assertTrue(maxTime <= 1.0)
    }

    @Test
    fun computeMaxTime_constantUv_whenAlreadyBurned_returnsZero() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            sunUnitsSoFar = SunburnCalculator.maxSunUnits,
            skinType = 3,
            spf = 1,
            altitudeInKm = 7000,
            isOnSnowOrWater = false
        )

        assertEquals(0.0, maxTime, delta)
    }

    @Test
    fun computeMaxTime_constantUv_whenAlmostBurned_returnsOneOrLess() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 5.0,
            sunUnitsSoFar = SunburnCalculator.maxSunUnits - 0.1,
            skinType = 3,
            spf = 1,
            altitudeInKm = 7000,
            isOnSnowOrWater = false
        )

        assertTrue(maxTime > 0.0)
        assertTrue(maxTime <= 1.0)
    }

    @Test
    fun computeMaxTime_constantUv_whenBurnUnlikely_returnsMaxValue() {
        val maxTime = SunburnCalculator.computeMaxTime(
            uvIndex = 1.0,
            sunUnitsSoFar = 0.0,
            skinType = 6,
            spf = 50,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(SunburnCalculator.NO_BURN_EXPECTED, maxTime, delta)
    }

    @Test
    fun computeMaxTime_variableUv_whenBurnUnlikely_returnsMaxValue() {
        val elevenAmPrediction = UvPredictionPoint(LocalTime.NOON.minusHours(1), 1.0)
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 2.0)
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 1.0)
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)

        val maxTime = SunburnCalculator.computeMaxTime(
            uvPrediction = prediction,
            currentTime = LocalTime.NOON.minusHours(1),
            sunUnitsSoFar = 0.0,
            skinType = 6,
            spf = 50,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )

        assertEquals(SunburnCalculator.NO_BURN_EXPECTED, maxTime, delta)
    }

    @Test
    fun computeSunUnitsInOneMinute_whenUvIndexIs0_returns0() {
        val sunUnits = SunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = 0.0,
            skinType = 5,
            spf = 50
        )

        assertEquals(0.0, sunUnits, 0.001)
    }
}