package com.androidandrew.sunscreen.domain

import com.androidandrew.sunscreen.model.ClothingBottom
import com.androidandrew.sunscreen.model.ClothingTop
import com.androidandrew.sunscreen.model.UserClothing
import org.junit.Assert.assertEquals
import org.junit.Test

class UvFactorTest {

    @Test
    fun getSkinExposedFactor_fromClothing_followsRuleOfNines() {
        val delta = 0.01
        val leewayDelta = 0.05

        // Naked is close to 100% skin exposed
        var clothing = UserClothing(top = ClothingTop.NOTHING, bottom = ClothingBottom.NOTHING)
        assertEquals(1.0, UvFactor.getSkinExposedFactor(clothing), delta)

        // Shorts have some leeway
        clothing = UserClothing(top = ClothingTop.NOTHING, bottom = ClothingBottom.SHORTS)
        assertEquals(1.0 - 0.18, UvFactor.getSkinExposedFactor(clothing), leewayDelta)

        // Pants cover ~36% (18 * 2)
        clothing = UserClothing(top = ClothingTop.NOTHING, bottom = ClothingBottom.PANTS)
        assertEquals(1.0 - 0.36, UvFactor.getSkinExposedFactor(clothing), delta)

        // Long-sleeve shirts cover ~54% (18 * 3)
        clothing = UserClothing(top = ClothingTop.LONG_SLEEVE_SHIRT, bottom = ClothingBottom.NOTHING)
        assertEquals(1.0 - 0.54, UvFactor.getSkinExposedFactor(clothing), delta)

        // T-Shirts have some leeway
        clothing = UserClothing(top = ClothingTop.T_SHIRT, bottom = ClothingBottom.NOTHING)
        assertEquals(1.0 - 0.18, UvFactor.getSkinExposedFactor(clothing), leewayDelta)
    }

    @Test
    fun getAltitudeFactor() {
        val delta = 0.01
        assertEquals(1.0, UvFactor.getAltitudeFactor(0), delta)
        assertEquals(1.075, UvFactor.getAltitudeFactor(500), delta)
        assertEquals(1.15, UvFactor.getAltitudeFactor(1000), delta)
        assertEquals(1.3, UvFactor.getAltitudeFactor(2000), delta)
        assertEquals(2.5, UvFactor.getAltitudeFactor(10000), delta)
    }

    @Test
    fun getReflectionFactor_isDoubled_onSnowOrWater() {
        val noReflection = UvFactor.getReflectionFactor(isReflective = false)
        val withReflection = UvFactor.getReflectionFactor(isReflective = true)
        assertEquals(withReflection, 2 * noReflection)
    }
}