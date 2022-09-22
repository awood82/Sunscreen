package com.androidandrew.sunscreen.tracker.uv

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class UvPredictionKtTest {

    private val elevenAmPrediction = UvPredictionPoint(LocalTime.NOON.minusHours(1), 8.0)
    private val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0)
    private val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 6.0)
    private val delta = 0.1
    private val midnight = LocalTime.MIDNIGHT
    private val sixAm = LocalTime.NOON.minusHours(6)
    private val elevenAm = LocalTime.NOON.minusHours(1)
    private val elevenFifteen = LocalTime.NOON.minusMinutes(45)
    private val noon = LocalTime.NOON
    private val twelve30 = LocalTime.NOON.plusMinutes(30)
    private val twelve45 = LocalTime.NOON.plusMinutes(45)
    private val one = LocalTime.NOON.plusHours(1)
    private val sixPm = LocalTime.NOON.plusHours(6)

    @Test
    fun getNearestPoints_withEmptyList_returnsNoPrediction() {
        val prediction = emptyList<UvPredictionPoint>()

        val points = prediction.getNearestPoints(midnight)

        assertEquals(NO_PREDICTION_BEFORE, points[0])
        assertEquals(NO_PREDICTION_AFTER, points[1])
    }

    @Test
    fun getNearestPoints_withOnePoint_returnsThatPoint() {
        val prediction = listOf(noonPrediction)

        val points = prediction.getNearestPoints(midnight)

        assertEquals(NO_PREDICTION_BEFORE, points[0])
        assertEquals(noonPrediction, points[1])
    }

    @Test
    fun getNearestPoints_withTwoPoints_andCurrentTimeIsBefore_returnsEarliestPoint() {
        val prediction = listOf(noonPrediction, onePmPrediction)
        val eleven = LocalTime.NOON.minusHours(1)

        val points = prediction.getNearestPoints(eleven)

        assertEquals(NO_PREDICTION_BEFORE, points[0])
        assertEquals(noonPrediction, points[1])
    }

    @Test
    fun getNearestPoints_withTwoPoints_andCurrentTimeIsAfter_returnsLatestPoint() {
        val prediction = listOf(elevenAmPrediction, noonPrediction)

        val points = prediction.getNearestPoints(one)

        assertEquals(noonPrediction, points[0])
        assertEquals(NO_PREDICTION_AFTER, points[1])
    }

    @Test
    fun getNearestPoints_withTwoPoints_andCurrentTimeIsInThatRange_returnsThosePoints() {
        val prediction = listOf(noonPrediction, onePmPrediction)

        val points = prediction.getNearestPoints(twelve30)

        assertEquals(noonPrediction, points[0])
        assertEquals(onePmPrediction, points[1])
    }

    @Test
    fun getNearestPoints_withTwoPoints_andCurrentTimeMatchesOnePoint_returnsPointsInRange() {
        val prediction = listOf(noonPrediction, onePmPrediction)

        val points = prediction.getNearestPoints(noon)

        assertEquals(noonPrediction, points[0])
        assertEquals(onePmPrediction, points[1])
    }

    @Test
    fun getNearestPoints_withThreePoints_andCurrentTimeIsInRange_returnsPointsInRange() {
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)

        val points = prediction.getNearestPoints(twelve30)

        assertEquals(noonPrediction, points[0])
        assertEquals(onePmPrediction, points[1])
    }

    @Test
    fun getUvNow_betweenTwoPoints_returnsAverage() {
        val prediction = listOf(noonPrediction, onePmPrediction)

        val uv = prediction.getUvNow(twelve30)

        val averageUv = (noonPrediction.uvIndex + onePmPrediction.uvIndex) / 2
        assertEquals(averageUv, uv, delta)
    }

    @Test
    fun getUvNow_risingUv_andTimeCloserToEarlier_returnsValueCloserToEarlier() {
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)

        val uv = prediction.getUvNow(elevenFifteen)

        // 8.0 @ 11a, 10.0 @ 12p
        assertEquals(8.5, uv, delta)
    }

    @Test
    fun getUvNow_fallingUv_andTimeCloserToLater_returnsValueCloserToLater() {
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)

        val uv = prediction.getUvNow(twelve45)

        // 10.0 @ 12p, 6.0 @ 1p
        assertEquals(7.0, uv, delta)
    }
}