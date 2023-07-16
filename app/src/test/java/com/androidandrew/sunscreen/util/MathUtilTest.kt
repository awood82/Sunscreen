package com.androidandrew.sunscreen.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class MathUtilTest {

    @Test
    fun percentToInt_returnsExpectedValues() {
        assertEquals(0, 0.0f.percentToInt())
        assertEquals(10, 0.1f.percentToInt())
        assertEquals(50, 0.5f.percentToInt())
        assertEquals(100, 1.0f.percentToInt())
        assertEquals(1000, 10.0f.percentToInt())
        assertEquals(-100, (-1.0f).percentToInt())
    }
}