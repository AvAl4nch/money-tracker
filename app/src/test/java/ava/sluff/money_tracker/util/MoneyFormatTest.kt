package ava.sluff.money_tracker.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatTest {

    @Test
    fun `formats with three decimals and grouping regardless of locale`() {
        assertEquals("3,287.075", MoneyFormat.amount(3287.075))
        assertEquals("0.700", MoneyFormat.amount(0.7))
        assertEquals("1,234,567.890", MoneyFormat.amount(1234567.89))
    }
}
