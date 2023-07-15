package com.androidandrew.sunscreen.analytics

import android.os.Bundle
import com.androidandrew.sunscreen.model.UserClothing
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.concurrent.TimeUnit

class FirebaseEventLogger(
    private val firebaseAnalytics: FirebaseAnalytics
) : EventLogger {

    private var trackingStartTimeMs: Long? = null

    override fun startTutorial() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, null)
    }

    override fun finishTutorial() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, null)
    }

    override fun viewScreen(name: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
        })
    }

    override fun selectLocation(location: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, ContentType.LOCATION.name)
            putString(FirebaseAnalytics.Param.ITEM_ID, location)
        })
    }

    override fun selectSkinType(skinType: Int) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, ContentType.SKIN_TYPE.name)
            putString(FirebaseAnalytics.Param.ITEM_ID, skinType.toString())
        })
    }

    override fun selectClothing(clothing: UserClothing) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, ContentType.TOP.name)
            putString(FirebaseAnalytics.Param.ITEM_ID, clothing.top.name)
        })
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, ContentType.BOTTOM.name)
            putString(FirebaseAnalytics.Param.ITEM_ID, clothing.bottom.name)
        })
    }

    override fun selectSpf(spf: Int) {
        firebaseAnalytics.logEvent(Event.SELECT_SPF.name, Bundle().apply {
            putInt(Param.SPF.name, spf)
        })
    }

    override fun selectReflectiveSurface(isReflective: Boolean) {
        firebaseAnalytics.logEvent(Event.SELECT_REFLECTIVE_SURFACE.name, Bundle().apply {
            putBoolean(Param.IS_REFLECTIVE.name, isReflective)
        })
    }

    override fun searchLocation(location: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, location)
        })
    }

    override fun searchSuccess(location: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event. VIEW_SEARCH_RESULTS, Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, location)
        })
    }

    override fun searchError(location: String, error: String?) {
        // TODO: Add Crashlytics?
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event. VIEW_SEARCH_RESULTS, Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, location)
            putString(Param.ERROR_LOCATION.name, location)
            putString(Param.ERROR.name, error)
        })
    }

    override fun startTracking() {
        // TODO: What to log? Time? Burn and IU?
        trackingStartTimeMs = System.currentTimeMillis()
        firebaseAnalytics.logEvent(Param.TRACKING_START.name, null)
    }

    override fun finishTracking() {
        // TODO: What to log? Time? Burn and IU?
        val elapsedTime = trackingStartTimeMs?.let {
            TimeUnit.MILLISECONDS.toMinutes((System.currentTimeMillis() - it))
        } ?: -1
        trackingStartTimeMs = null
        firebaseAnalytics.logEvent(Param.TRACKING_FINISH.name, Bundle().apply {
            putLong("time", elapsedTime)
        })
    }
}

private enum class Event(name: String) {
    SELECT_REFLECTIVE_SURFACE("select_reflective_surface"),
    SELECT_SPF("select_spf"),
}

private enum class Param(name: String) {
    ERROR("error"),
    ERROR_LOCATION("error_location"),
    IS_REFLECTIVE("is_reflective"),
    SPF("spf"),
    TRACKING_START("tracking_start"),
    TRACKING_FINISH("tracking_finish")
}

private enum class ContentType(name: String) {
    LOCATION("location"),
    SKIN_TYPE("skin_type"),
    TOP("top"),
    BOTTOM("bottom")
}