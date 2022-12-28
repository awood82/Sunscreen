package com.androidandrew.sunscreen.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertSpfUseCaseTest {
    
    private val spfUseCase = ConvertSpfUseCase()
    
    @Test
    fun forCalculations_clampsBetween1and50() {
        assertEquals(
            ConvertSpfUseCase.MIN_SPF, spfUseCase.forCalculations(
                ConvertSpfUseCase.MIN_SPF))
        assertEquals(
            ConvertSpfUseCase.MIN_SPF, spfUseCase.forCalculations(
                ConvertSpfUseCase.MIN_SPF - 1))
        assertEquals(
            ConvertSpfUseCase.MIN_SPF, spfUseCase.forCalculations(
                ConvertSpfUseCase.MIN_SPF - 2))

        assertEquals(
            ConvertSpfUseCase.MAX_SPF, spfUseCase.forCalculations(
                ConvertSpfUseCase.MAX_SPF))
        assertEquals(
            ConvertSpfUseCase.MAX_SPF, spfUseCase.forCalculations(
                ConvertSpfUseCase.MAX_SPF + 1))

        assertEquals(ConvertSpfUseCase.MIN_SPF, spfUseCase.forCalculations(null))
    }

    @Test
    fun forDisplay_returnsExpected() {
        assertEquals("5", spfUseCase.forDisplay(5))
        assertEquals("0", spfUseCase.forDisplay(0))
        assertEquals("0", spfUseCase.forDisplay(null))
    }
}