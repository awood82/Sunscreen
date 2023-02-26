package com.androidandrew.sunscreen.analytics

import com.androidandrew.sunscreen.model.UserClothing

interface EventLogger {

    fun startTutorial()
    fun finishTutorial()

    fun viewScreen(name: String)

    fun searchLocation(location: String)
    fun selectSkinType(skinType: Int)
    fun selectClothing(clothing: UserClothing)
}