package com.androidandrew.sunscreen.domain

import com.androidandrew.sunscreen.model.ClothingBottom
import com.androidandrew.sunscreen.model.ClothingTop
import com.androidandrew.sunscreen.model.UserClothing

object UvFactor {

    enum class Clothing {
        NAKED,
        SHORTS_NO_SHIRT,
        PANTS_NO_SHIRT,
        SHORTS_T_SHIRT,
        PANTS_T_SHIRT,
        SHORTS_LONG_SLEEVE_SHIRT,
        PANTS_LONG_SLEEVE_SHIRT
    }

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

    @Deprecated(
        message = "Use the function that takes a UserClothing argument instead",
        replaceWith = ReplaceWith("getSkinExposedFactor")
    )
    fun getSkinExposedFactor(clothing: Clothing): Double {
        return when (clothing) {
            Clothing.NAKED -> 100.0
            Clothing.SHORTS_NO_SHIRT -> 82.0
            Clothing.PANTS_NO_SHIRT -> 64.0
            Clothing.SHORTS_T_SHIRT -> 40.0
            Clothing.PANTS_T_SHIRT -> 23.0
            Clothing.SHORTS_LONG_SLEEVE_SHIRT -> 27.0
            Clothing.PANTS_LONG_SLEEVE_SHIRT -> 11.0
        }
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