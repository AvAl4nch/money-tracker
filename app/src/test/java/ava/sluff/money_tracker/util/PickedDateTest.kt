package ava.sluff.money_tracker.util

import org.junit.Assert.assertEquals
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import org.junit.Test

class PickedDateTest {

    private fun localMillis(
        year: Int, month: Int, day: Int, hour: Int, minute: Int, zone: TimeZone
    ): Long = Calendar.getInstance(zone, Locale.US).apply {
        clear()
        set(year, month - 1, day, hour, minute, 0)
    }.timeInMillis

    private fun utcMidnight(year: Int, month: Int, day: Int): Long =
        localMillis(year, month, day, 0, 0, TimeZone.getTimeZone("UTC"))

    private fun fieldsIn(zone: TimeZone, millis: Long): List<Int> =
        Calendar.getInstance(zone, Locale.US).apply { timeInMillis = millis }.let {
            listOf(
                it.get(Calendar.YEAR),
                it.get(Calendar.MONTH) + 1,
                it.get(Calendar.DAY_OF_MONTH),
                it.get(Calendar.HOUR_OF_DAY),
                it.get(Calendar.MINUTE)
            )
        }

    @Test
    fun `keeps the original time of day when only the date changes`() {
        val amman = TimeZone.getTimeZone("Asia/Amman")
        val original = localMillis(2026, 8, 12, 14, 32, amman)

        val moved = PickedDate.applyTo(original, utcMidnight(2026, 8, 20), amman)

        assertEquals(listOf(2026, 8, 20, 14, 32), fieldsIn(amman, moved))
    }

    @Test
    fun `lands on the picked calendar day in zones behind UTC`() {
        val denver = TimeZone.getTimeZone("America/Denver")
        val original = localMillis(2026, 8, 12, 9, 5, denver)

        val moved = PickedDate.applyTo(original, utcMidnight(2026, 8, 20), denver)

        assertEquals(listOf(2026, 8, 20, 9, 5), fieldsIn(denver, moved))
    }

    @Test
    fun `does not shift a transaction into the previous month`() {
        val denver = TimeZone.getTimeZone("America/Denver")
        val original = localMillis(2026, 8, 15, 23, 50, denver)

        val moved = PickedDate.applyTo(original, utcMidnight(2026, 8, 1), denver)

        assertEquals(listOf(2026, 8, 1, 23, 50), fieldsIn(denver, moved))
    }
}
