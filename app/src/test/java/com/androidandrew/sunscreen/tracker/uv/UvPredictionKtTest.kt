package com.androidandrew.sunscreen.tracker.uv

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class UvPredictionKtTest {

    private val elevenAmPrediction = UvPredictionPoint(LocalTime.NOON.minusHours(1), 8.0)
    private val noonPrediction = UvPredictionPoint(LocalTime.NOON, 10.0)
    private val onePmPrediction = UvPredictionPoint(LocalTime.NOON.plusHours(1), 9.0)

    @Test
    fun getNearestPoints_withEmptyList_returnsNoPrediction() {
        val prediction = emptyList<UvPredictionPoint>()

        val points = prediction.getNearestPoints(LocalTime.MIDNIGHT)

        assertEquals(NO_PREDICTION_BEFORE, points[0])
        assertEquals(NO_PREDICTION_AFTER, points[1])
    }

    @Test
    fun getNearestPoints_withOnePoint_returnsThatPoint() {
        val prediction = listOf(noonPrediction)

        val points = prediction.getNearestPoints(LocalTime.MIDNIGHT)

        assertEquals(NO_PREDICTION_BEFORE, points[0])
        assertEquals(noonPrediction, points[1])
    }

    @Test
    fun getNearestPoints_withTwoPoints_andCurrentTimeIsBefore_returnsEarliestPoint() {
        val prediction = listOf(noonPrediction, onePmPrediction)

        val points = prediction.getNearestPoints(LocalTime.NOON.minusHours(1))

        assertEquals(NO_PREDICTION_BEFORE, points[0])
        assertEquals(noonPrediction, points[1])
    }

    @Test
    fun getNearestPoints_withTwoPoints_andCurrentTimeIsAfter_returnsLatestPoint() {
        val prediction = listOf(elevenAmPrediction, noonPrediction)

        val points = prediction.getNearestPoints(LocalTime.NOON.plusHours(1))

        assertEquals(noonPrediction, points[0])
        assertEquals(NO_PREDICTION_AFTER, points[1])
    }

    @Test
    fun getNearestPoints_withTwoPoints_andCurrentTimeIsInThatRange_returnsThosePoints() {
        val prediction = listOf(noonPrediction, onePmPrediction)

        val points = prediction.getNearestPoints(LocalTime.NOON.plusMinutes(30))

        assertEquals(noonPrediction, points[0])
        assertEquals(onePmPrediction, points[1])
    }

    @Test
    fun getNearestPoints_withThreePoints_andCurrentTimeIsInRange_returnsPointsInRange() {
        val prediction = listOf(elevenAmPrediction, noonPrediction, onePmPrediction)

        val points = prediction.getNearestPoints(LocalTime.NOON.plusMinutes(30))

        assertEquals(noonPrediction, points[0])
        assertEquals(onePmPrediction, points[1])
    }
}