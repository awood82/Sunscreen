package com.androidandrew.sunscreen.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserClothingTest {

    @Test
    fun toUserClothing_whenInvalid_mapsToDefaults() {
        val actual = 99.toUserClothing()

        assertEquals(defaultUserClothing, actual)
    }

    @Test
    fun toUserClothing_whenOneIsSet_otherMapsToDefaults() {
        var actual = 19.toUserClothing()
        var expected = UserClothing(top = ClothingTop.T_SHIRT, bottom = defaultBottom)
        assertEquals(expected, actual)

        actual = 91.toUserClothing()
        expected = UserClothing(top = defaultTop, bottom = ClothingBottom.SHORTS)
        assertEquals(expected, actual)
    }

    @Test
    fun toUserClothing_whenBothAreSet_mapsCorrectly() {
        var actual = 0.toUserClothing()
        var expected = UserClothing(top = ClothingTop.NOTHING, bottom = ClothingBottom.NOTHING)
        assertEquals(expected, actual)

        actual = 1.toUserClothing()
        expected = UserClothing(top = ClothingTop.NOTHING, bottom = ClothingBottom.SHORTS)
        assertEquals(expected, actual)

        actual = 2.toUserClothing()
        expected = UserClothing(top = ClothingTop.NOTHING, bottom = ClothingBottom.PANTS)
        assertEquals(expected, actual)

        actual = 10.toUserClothing()
        expected = UserClothing(top = ClothingTop.T_SHIRT, bottom = ClothingBottom.NOTHING)
        assertEquals(expected, actual)

        actual = 11.toUserClothing()
        expected = UserClothing(top = ClothingTop.T_SHIRT, bottom = ClothingBottom.SHORTS)
        assertEquals(expected, actual)

        actual = 12.toUserClothing()
        expected = UserClothing(top = ClothingTop.T_SHIRT, bottom = ClothingBottom.PANTS)
        assertEquals(expected, actual)

        actual = 20.toUserClothing()
        expected = UserClothing(top = ClothingTop.LONG_SLEEVE_SHIRT, bottom = ClothingBottom.NOTHING)
        assertEquals(expected, actual)

        actual = 21.toUserClothing()
        expected = UserClothing(top = ClothingTop.LONG_SLEEVE_SHIRT, bottom = ClothingBottom.SHORTS)
        assertEquals(expected, actual)

        actual = 22.toUserClothing()
        expected = UserClothing(top = ClothingTop.LONG_SLEEVE_SHIRT, bottom = ClothingBottom.PANTS)
        assertEquals(expected, actual)
    }

    @Test
    fun toDatabaseValue_mapsCorrectly() {
        var actual = UserClothing(top = ClothingTop.NOTHING, bottom = ClothingBottom.NOTHING).toDatabaseValue()
        var expected = 0
        assertEquals(expected, actual)

        actual = UserClothing(top = ClothingTop.T_SHIRT, bottom = ClothingBottom.SHORTS).toDatabaseValue()
        expected = 11
        assertEquals(expected, actual)

        actual = UserClothing(top = ClothingTop.LONG_SLEEVE_SHIRT, bottom = ClothingBottom.PANTS).toDatabaseValue()
        expected = 22
        assertEquals(expected, actual)
    }
}