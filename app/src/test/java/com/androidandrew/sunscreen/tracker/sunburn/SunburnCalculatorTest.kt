package com.androidandrew.sunscreen.tracker.sunburn

import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import kotlin.math.max

class SunburnCalculatorTest {

    private val calc = SunburnCalculator()
    private val delta = 0.1

    // A baseline test to compare to the omni calculator website
    @Test
    fun computeMaxTime_withSkinType4_andUV5_is60Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(60.0, maxTime, delta)
    }

    // Being on snow or in the water half the maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType4_andUV5_andReflectiveSurface_is30Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = true)

        assertEquals(30.0, maxTime, delta)
    }

    // Higher UV index reduces the maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType4_andUV10_is30Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvIndex = 10.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(30.0, maxTime, delta)
    }

    // Wearing sunscreen w/ SPF increases the maximum sun exposure time linearly
    @Test
    fun computeMaxTime_withSkinType4_andUV5_andSPF10_is600Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvIndex = 5.0,
            spf = 10,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(600.0, maxTime, delta)
    }

    // Having darker color skin increases maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType5_andUV5_is80Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 5,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(80.0, maxTime, delta)
    }

    // Having darker color skin increases maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType6_andUV5_is100Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 6,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(100.0, maxTime, delta)
    }

    // Having lighter color skin reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType3_andUV5_is30Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 3,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(40.0, maxTime, delta)
    }

    // Having lighter color skin reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType2_andUV5_is20Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 2,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(20.0, maxTime, delta)
    }

    // Having lighter color skin reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType1_andUV5_is13_3Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 1,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(13.33, maxTime, delta)
    }

    // Higher altitude reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType3_andUV5_at2km_is30Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 3,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 2000,
            isOnSnowOrWater = false)

        assertEquals(30.7, maxTime, delta)
    }

    // Higher altitude reduces maximum sun exposure time
    @Test
    fun computeMaxTime_withSkinType3_andUV5_at7km_is19Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 3,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 7000,
            isOnSnowOrWater = false)

        assertEquals(19.5, maxTime, delta)
    }

    // If skintype isn't set, we can't compute
    @Test
    fun computeMaxTime_withSkinTypeUnknown_is0Minutes() {
        val maxTime = calc.computeMaxTime(
            skinType = 0,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = true)

        assertEquals(0.0, maxTime, delta)
    }

    // If the max time is 20 minutes, then the sun units in one minute is 5. 100% / 20min = 5%/min
    @Test
    fun computeSunUnits_withMaxTime20Minutes_is5Percent() {
        val sunUnits = calc.computeSunUnitsInOneMinute(
            skinType = 2,
            uvIndex = 5.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(5.0, sunUnits, delta)
    }

    // maxTime is 30 minutes w/ constant UV, so it should be less w/ rising UV
    @Test
    fun computeMaxTime_withRisingUv_overFullHour_takesAverage() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 5.0) // 60 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 10.0) // 30 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(45.0, maxTime, delta)
    }

    // maxTime is 30 minutes w/ constant UV, so it should be more w/ falling UV
    @Test
    fun computeMaxTime_withFallingUv_isLessThanAverage_overFullHour() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        // average of 60 and 30 is 45.0
        assertTrue(maxTime < 45.0)
    }

    @Test
    fun computeMaxTime_withSunUnitsSoFar_usesThem() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 10.0) // 30 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            sunUnitsSoFar = 50.0, // halfway, so burn time should be half of 30
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(15.0, maxTime, delta)
    }

    @Test
    fun computeMaxTime_betweenHalfHours() {
        val elevenAmPrediction = UvPredictionPoint(LocalTime.NOON.minusHours(1), 2.5) // 120 minutes to burn
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 3.0) // 100 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 2.5) // 120 minutes to burn
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)
        val currentTime = LocalTime.NOON.minusMinutes(30) // 11:30 am

        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvPrediction = prediction,
            currentTime = currentTime,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(110.0, maxTime, delta)
    }

    @Test
    fun computeMaxTime_withInsaneUv_sameForRisingToApex_asFallingFromApex() {
        val elevenAmPrediction = UvPredictionPoint(LocalTime.NOON.minusHours(1), 5.0) // 60 minutes to burn
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 150.0) // 2 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)

        val rising = calc.computeMaxTime(
            skinType = 4,
            uvPrediction = prediction,
            currentTime = LocalTime.NOON.minusMinutes(3),
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        val falling = calc.computeMaxTime(
            skinType = 4,
            uvPrediction = prediction,
            currentTime = LocalTime.NOON.plusMinutes(3),
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(rising, falling, delta)
    }

    @Test
    fun computeMaxTime_whenAlreadyBurned_isZero() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            sunUnitsSoFar = 100.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(0.0, maxTime, delta)
    }

    fun computeMaxTime_whenAlmostBurned_isOne() {
        val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0) // 30 minutes to burn
        val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 5.0) // 60 minutes to burn
        val prediction = listOf(noonPrediction, onePmPrediction)

        val maxTime = calc.computeMaxTime(
            skinType = 4,
            uvPrediction = prediction,
            currentTime = LocalTime.NOON,
            sunUnitsSoFar = 99.0,
            spf = 1,
            altitudeInKm = 0,
            isOnSnowOrWater = false)

        assertEquals(1.0, maxTime, delta)
    }
}