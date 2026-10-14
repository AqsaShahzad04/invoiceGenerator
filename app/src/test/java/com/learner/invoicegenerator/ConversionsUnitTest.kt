package com.learner.invoicegenerator

import com.learner.invoicegenerator.util.conversions
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversionsUnitTest {

    @Test
    fun calculatePercentageChange_zeroPreviousMonth_returnsCorrectValue() {
        assertEquals(100.0, conversions.calculatePercentageChange(500.0, 0.0), 0.001)
        assertEquals(0.0, conversions.calculatePercentageChange(0.0, 0.0), 0.001)
    }

    @Test
    fun calculatePercentageChange_positiveIncrease_returnsPositivePercentage() {
        assertEquals(100.0, conversions.calculatePercentageChange(200.0, 100.0), 0.001)
        assertEquals(50.0, conversions.calculatePercentageChange(150.0, 100.0), 0.001)
    }

    @Test
    fun calculatePercentageChange_decrease_returnsNegativePercentage() {
        assertEquals(-50.0, conversions.calculatePercentageChange(50.0, 100.0), 0.001)
    }

    @Test
    fun formatAmount_wholeNumber_returnsNoDecimals() {
        assertEquals("500", conversions.formatAmount(500.0))
        assertEquals("1200", conversions.formatAmount(1200.00))
    }

    @Test
    fun formatAmount_decimalNumber_trimsTrailingZeros() {
        assertEquals("500.5", conversions.formatAmount(500.50))
        assertEquals("1200.25", conversions.formatAmount(1200.25))
    }
}
