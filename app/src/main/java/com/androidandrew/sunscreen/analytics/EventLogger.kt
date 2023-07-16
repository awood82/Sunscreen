package com.androidandrew.sunscreen.analytics

import com.androidandrew.sunscreen.model.UserClothing

interface EventLogger {

    fun startTutorial()
    fun finishTutorial()

    fun viewScreen(name: String)

    fun selectLocation(location: String)
    fun selectSkinType(skinType: Int)
    fun selectClothing(clothing: UserClothing)

    fun searchLocation(location: String)
    fun searchSuccess(location: String)
    fun searchError(location: String, error: String?)

    fun selectSpf(spf: Int)
    fun selectReflectiveSurface(isReflective: Boolean)

    /**
     * Log the current time, sunburn, and vitamin D
     * @param currentTimeInMillis System time in milliseconds
     * @param currentSunburnPercent0to1 Current sunburn as a float where 0.0f = 0% and 1.0f = 100%
     * @param currentVitaminDPercent0to1 Current vitamin D as a float where 0.0f = 0% and 1.0f = 100%
     */
    fun startTracking(
        currentTimeInMillis: Long,
        currentSunburnPercent0to1: Float,
        currentVitaminDPercent0to1: Float
    )

    /**
     * Log the elapsed time, sunburn, and vitamin D since the tracking started
     * @param currentTimeInMillis System time in milliseconds
     * @param currentSunburnPercent0to1 Current sunburn as a float where 0.0f = 0% and 1.0f = 100%
     * @param currentVitaminDPercent0to1 Current vitamin D as a float where 0.0f = 0% and 1.0f = 100%
     */
    fun finishTracking(
        currentTimeInMillis: Long,
        currentSunburnPercent0to1: Float,
        currentVitaminDPercent0to1: Float
    )
}