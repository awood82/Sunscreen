package com.androidandrew.sunscreen.tracker.uv

import java.time.Duration
import java.time.LocalTime

data class UvPredictionPoint(val time: LocalTime, val uvIndex: Double)

typealias UvPrediction = List<UvPredictionPoint>
val NO_PREDICTION_BEFORE = UvPredictionPoint(time = LocalTime.MIDNIGHT, uvIndex = 0.0)
val NO_PREDICTION_AFTER = UvPredictionPoint(time = LocalTime.MIDNIGHT.plusHours(23).plusMinutes(59), uvIndex = 0.0)

/***
 * Returns a List w/ 2 elements with the current hour and next hour's predictions.
 * Prerequisite: UvPredictionPoints are in sorted, ascending order by time.
 */
fun UvPrediction.getNearestPoints(currentTime: LocalTime): List<UvPredictionPoint> {
    val returnList = this.toMutableList()
    returnList.add(0, NO_PREDICTION_BEFORE)
    returnList.add(NO_PREDICTION_AFTER)

    var returnIndex = 0
    for (i in 0..returnList.size) {
        if (currentTime.isAfter(returnList[i].time)) {
            returnIndex = i
        } else {
            break
        }
    }
    return returnList.subList(returnIndex, returnIndex + 2)
}

// Assumes a linear progression over an hour of UV prediction points.
fun UvPrediction.getUvNow(currentTime: LocalTime): Double {
    val prediction = getNearestPoints(currentTime)
    val sinceStart = Duration.between(prediction[0].time, currentTime)
    val totalDuration = Duration.between(prediction[0].time, prediction[1].time) // usually an hour
    val progress = sinceStart.toMillis().toDouble() / totalDuration.toMillis() // e.g. 25% past the hour
    val uvDifference = prediction[1].uvIndex - prediction[0].uvIndex
    val uvAdjustment = progress * uvDifference

    return prediction[0].uvIndex + uvAdjustment
}