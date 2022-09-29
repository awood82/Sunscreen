package com.androidandrew.sunscreen.tracker

object UvFactor {

    enum class Clothing {
        NAKED,
        SHORTS_NO_SHIRT,
        PANTS_NO_SHIRT,
        SHORTS_T_SHIRT,
        PANTS_T_SHIRT,
        PANTS_LONG_SLEEVE_SHIRT,
    }

    fun getSkinExposedFactor(clothing: Clothing): Double {
        return when (clothing) {
            Clothing.NAKED -> 100.0
            Clothing.SHORTS_NO_SHIRT -> 88.0
            Clothing.PANTS_NO_SHIRT -> 50.0
            Clothing.SHORTS_T_SHIRT -> 35.0
            Clothing.PANTS_T_SHIRT -> 25.0
            Clothing.PANTS_LONG_SLEEVE_SHIRT -> 11.0
        }
    }

    fun getSkinBlockFactor(type: Int): Int {
        return when(type) {
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
        return when(isReflective) {
            false -> 1
            true -> 2
        }
    }

    fun getAltitudeFactor(altitudeInKm: Int): Double {
        return 1.0 + 0.15 * (altitudeInKm / 1000.0)
    }
}