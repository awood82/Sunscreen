package com.androidandrew.sunscreen.domain

import com.androidandrew.sunscreen.model.ClothingBottom
import com.androidandrew.sunscreen.model.ClothingTop
import com.androidandrew.sunscreen.model.UserClothing

object UvFactor {

    /**
     * Use "Rule of 9s" to approximate % of body
     * Head and neck = 9%
     * Each arm = 9% (2)
     * Each leg = 18% (2)
     * Chest and upper back = 18%
     * Abs and lower back = 18%
     * So shorts cover ~18%, pants ~36%, T-shirt ~40%, long-sleeve shirt ~54%
     * @return In range [0, 100]
     */
    fun getSkinExposedFactor(clothing: UserClothing): Double {
        var exposed = 99.0 // The last 1% is usually covered
        when (clothing.top) {
            ClothingTop.NOTHING -> {}
            ClothingTop.T_SHIRT -> exposed -= 40  // Some leeway allowed here. 36% plus a bit for sleeves
            ClothingTop.LONG_SLEEVE_SHIRT -> exposed -= 54
        }
        when (clothing.bottom) {
            ClothingBottom.NOTHING -> {}
            ClothingBottom.SHORTS -> exposed -= 18  // Some leeway allowed here
            ClothingBottom.PANTS -> exposed -= 36
        }
        return exposed
    }

    fun getSkinBlockFactor(type: Int): Int {
        return when (type) {
            1 -> 2
            2 -> 3
            3 -> 6
            4 -> 9
            5 -> 12
            6 -> 15
            else -> 0
        }
    }

    fun getReflectionFactor(isReflective: Boolean): Int {
        return when (isReflective) {
            false -> 1
            true -> 2
        }
    }

    fun getAltitudeFactor(altitudeInKm: Int): Double {
        return 1.0 + 0.15 * (altitudeInKm / 1000.0)
    }
}