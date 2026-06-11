package ava.sluff.money_tracker.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class MonthsTest {

    private fun calAt(window: Long): Calendar =
        Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = window }

    @Test
    fun `window covers exactly one month`() {
        val w = Months.window(2026, Calendar.JUNE)
        val start = calAt(w.startMs)
        assertEquals(2026, start.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, start.get(Calendar.MONTH))
        assertEquals(1, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
        val end = calAt(w.endMs)
        assertEquals(Calendar.JUNE, end.get(Calendar.MONTH))
        assertEquals(30, end.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, end.get(Calendar.HOUR_OF_DAY))
        assertEquals(w.endMs, Months.window(2026, Calendar.JULY).startMs - 1)
    }

    @Test
    fun `shift wraps across year boundaries`() {
        assertEquals(2025 to Calendar.DECEMBER, Months.shift(2026, Calendar.JANUARY, -1))
        assertEquals(2027 to Calendar.JANUARY, Months.shift(2026, Calendar.DECEMBER, +1))
    }

    @Test
    fun `label is human readable`() {
        assertEquals("June 2026", Months.label(2026, Calendar.JUNE))
    }

    @Test
    fun `lastMonths returns n descending-recent windows ending at given month`() {
        val list = Months.lastMonths(2026, Calendar.JUNE, 6)
        assertEquals(6, list.size)
        assertEquals(2026 to Calendar.JUNE, list.last().let { it.year to it.month })
        assertEquals(2026 to Calendar.JANUARY, list.first().let { it.year to it.month })
    }
}
