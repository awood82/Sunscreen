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
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, name + "Screen")
        })
    }

    override fun selectLocation(location: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "location")
            putString(FirebaseAnalytics.Param.ITEM_ID, location)
        })
    }

    override fun selectSkinType(skinType: Int) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "skin_type")
            putString(FirebaseAnalytics.Param.ITEM_ID, skinType.toString())
        })
    }

    override fun selectClothing(clothing: UserClothing) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "top")
            putString(FirebaseAnalytics.Param.ITEM_ID, clothing.top.name)
        })
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "bottom")
            putString(FirebaseAnalytics.Param.ITEM_ID, clothing.bottom.name)
        })
    }

    override fun selectSpf(spf: Int) {
        firebaseAnalytics.logEvent("select_spf", Bundle().apply {
            putInt("spf", spf)
        })
    }

    override fun selectReflectiveSurface(isReflective: Boolean) {
        firebaseAnalytics.logEvent("select_reflective_surface", Bundle().apply {
            putBoolean("is_reflective", isReflective)
        })
    }

    override fun searchLocation(location: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, location)
        })
    }

    override fun searchSuccess(location: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS, Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, location)
        })
    }

    override fun searchError(location: String, error: String?) {
        // TODO: Add Crashlytics?
    }

    override fun startTracking() {
        // TODO: What to log? Time? Burn and IU?
        trackingStartTimeMs = System.currentTimeMillis()
        firebaseAnalytics.logEvent("tracking_start", null)
    }

    override fun finishTracking() {
        // TODO: What to log? Time? Burn and IU?
        val elapsedTime = trackingStartTimeMs?.let {
            TimeUnit.MILLISECONDS.toMinutes((System.currentTimeMillis() - it))
        } ?: -1
        trackingStartTimeMs = null
        firebaseAnalytics.logEvent("tracking_finish", Bundle().apply {
            putLong("time", elapsedTime)
        })
    }
}