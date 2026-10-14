package com.learner.invoicegenerator

import com.learner.invoicegenerator.data.local.entity.NumberingReset
import com.learner.invoicegenerator.data.local.entity.PaymentDueDateOffset
import com.learner.invoicegenerator.data.local.entity.PaymentMethods
import com.learner.invoicegenerator.data.local.entity.SettingsConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsConverterUnitTest {

    private val converter = SettingsConverter()

    @Test
    fun numberingReset_convertsCorrectly() {
        assertEquals("YEARLY", converter.fromNumberingReset(NumberingReset.YEARLY))
        assertEquals(NumberingReset.MONTHLY, converter.toNumberingReset("MONTHLY"))
    }

    @Test
    fun paymentDueDateOffset_convertsCorrectly() {
        assertEquals("NET14", converter.fromPaymentDueDateOffset(PaymentDueDateOffset.NET14))
        assertEquals(PaymentDueDateOffset.NET30, converter.toPaymentDueDateOffset("NET30"))
    }

    @Test
    fun paymentMethodsList_convertsCorrectly() {
        val list = mutableListOf(PaymentMethods.Cash, PaymentMethods.BANKTransfer, PaymentMethods.EasyPaisa)
        val str = converter.fromPaymentMethodsList(list)
        assertEquals("Cash,BANKTransfer,EasyPaisa", str)

        val restored = converter.toPaymentMethodsList(str)
        assertEquals(3, restored.size)
        assertEquals(PaymentMethods.Cash, restored[0])
        assertEquals(PaymentMethods.BANKTransfer, restored[1])
        assertEquals(PaymentMethods.EasyPaisa, restored[2])
    }

    @Test
    fun paymentMethodsList_blankInput_returnsEmptyMutableListWithoutCrash() {
        val emptyList = converter.toPaymentMethodsList("")
        assertTrue(emptyList.isEmpty())
        emptyList.add(PaymentMethods.Cash) // verify mutable
        assertEquals(1, emptyList.size)
    }

    @Test
    fun paymentMethodsList_unknownEnum_safelyFilters() {
        val str = "Cash,UNKNOWN_METHOD,Card"
        val restored = converter.toPaymentMethodsList(str)
        assertEquals(2, restored.size)
        assertEquals(PaymentMethods.Cash, restored[0])
        assertEquals(PaymentMethods.Card, restored[1])
    }
}
