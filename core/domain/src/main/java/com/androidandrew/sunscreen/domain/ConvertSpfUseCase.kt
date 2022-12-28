package com.androidandrew.sunscreen.domain

class ConvertSpfUseCase {

    companion object {
        const val MIN_SPF = 1
        const val MAX_SPF = 50
    }

    /**
     * SPF > 50 is effectively meaningless
     * SPF of 1 is used for skin in calculations
     */
    fun forCalculations(spf: Int?): Int {
        return when {
            spf == null -> MIN_SPF
            spf <= MIN_SPF -> MIN_SPF
            spf > MAX_SPF -> MAX_SPF
            else -> spf
        }
    }

    fun forDisplay(spf: Int?): String {
        return spf?.toString() ?: "0"
    }
}