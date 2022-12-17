package com.androidandrew.sharedtest.model

import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sunscreen.model.uv.asUvPrediction

object FakeUvPredictions {

    val forecast = FakeEpaService.forecast.asUvPrediction()
}