package com.androidandrew.sunscreen.tracker.uv

import com.androidandrew.sunscreen.model.uv.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class UvPredictionTest {

    private val midnight = LocalTime.MIDNIGHT
    private val fiveAm = LocalTime.NOON.minusHours(7)
    private val sixAm = LocalTime.NOON.minusHours(6)
    private val elevenAm = LocalTime.NOON.minusHours(1)
    private val elevenFifteen = LocalTime.NOON.minusMinutes(45)
    private val noon = LocalTime.NOON
    private val twelve30 = LocalTime.NOON.plusMinutes(30)
    private val twelve45 = LocalTime.NOON.plusMinutes(45)
    private val onePm = LocalTime.NOON.plusHours(1)
    private val sixPm = LocalTime.NOON.plusHours(6)
    private val sevenPm = LocalTime.NOON.plusHours(7)

    private val fiveAmPrediction = UvPredictionPoint(fiveAm, 0.0)
    private val sixAmPrediction = UvPredictionPoint(sixAm, 0.0)
    private val elevenAmPrediction = UvPredictionPoint(elevenAm, 8.0)
    private val noonPrediction = UvPredictionPoint(noon, 10.0)
    private val onePmPrediction = UvPredictionPoint(onePm, 6.0)
    private val sixPmPrediction = UvPredictionPoint(sixPm, 0.0)
    private val sevenPmPrediction = UvPredictionPoint(sevenPm, 0.0)


    private val delta = 0.1

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

        val points = prediction.getNearestPoints(onePm)

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

    @Test
    fun trim_withNoZeroes_keepsList() {
        val prediction = listOf(
            elevenAmPrediction, noonPrediction, onePmPrediction
        )

        val trimmed = prediction.trim()

        assertEquals(3, trimmed.size)
        assertEquals(prediction, trimmed)
    }

    @Test
    fun trim_withLeadingZeroes_keepsOnlyOne() {
        val prediction = listOf(
            fiveAmPrediction, sixAmPrediction, // 0.0 UV index
            noonPrediction
        )

        val trimmed = prediction.trim()

        assertEquals(2, trimmed.size)
        assertEquals(sixAmPrediction, trimmed[0])
        assertEquals(noonPrediction, trimmed[1])
    }

    @Test
    fun trim_withTrailingZeroes_keepsOnlyOne() {
        val prediction = listOf(
            noonPrediction,
            sixPmPrediction, sevenPmPrediction // 0.0 UV index
        )

        val trimmed = prediction.trim()

        assertEquals(2, trimmed.size)
        assertEquals(noonPrediction, trimmed[0])
        assertEquals(sixPmPrediction, trimmed[1])
    }

    @Test
    fun trim_withLeadingAndTrailingZeroes_keepsOnlyOneLeadingAndOneTrailing() {
        val prediction = listOf(
            fiveAmPrediction, sixAmPrediction, // 0.0 UV index
            noonPrediction,
            sixPmPrediction, sevenPmPrediction // 0.0 UV index
        )

        val trimmed = prediction.trim()

        assertEquals(3, trimmed.size)
        assertEquals(sixAmPrediction, trimmed[0])
        assertEquals(noonPrediction, trimmed[1])
        assertEquals(sixPmPrediction, trimmed[2])
    }

    @Test
    fun trim_withAllZeroes_keepsAtLeastOfThem() {
        val prediction = listOf(
            fiveAmPrediction, sixAmPrediction, // 0.0 UV index
            sixPmPrediction, sevenPmPrediction // 0.0 UV index
        )

        val trimmed = prediction.trim()

        assertTrue(trimmed.isNotEmpty())
    }
}