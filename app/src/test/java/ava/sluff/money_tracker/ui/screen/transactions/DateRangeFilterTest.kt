package ava.sluff.money_tracker.ui.screen.transactions

import ava.sluff.money_tracker.util.Months
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateRangeFilterTest {

    @Test
    fun `all matches everything`() {
        assertTrue(DateRangeFilter.All.contains(0L))
        assertTrue(DateRangeFilter.All.contains(Long.MAX_VALUE))
    }

    @Test
    fun `this month bounds match Months window`() {
        val (y, m) = Months.current()
        val w = Months.window(y, m)
        val r = DateRangeFilter.ThisMonth.resolve()
        assertEquals(w.startMs, r.first)
        assertEquals(w.endMs, r.second)
    }

    @Test
    fun `last month is the month before current`() {
        val (y, m) = Months.current()
        val (py, pm) = Months.shift(y, m, -1)
        val w = Months.window(py, pm)
        val r = DateRangeFilter.LastMonth.resolve()
        assertEquals(w.startMs, r.first)
        assertEquals(w.endMs, r.second)
    }

    @Test
    fun `custom range is inclusive`() {
        val custom = DateRangeFilter.Custom(100L, 200L)
        assertTrue(custom.contains(100L))
        assertTrue(custom.contains(200L))
        assertTrue(!custom.contains(99L) && !custom.contains(201L))
    }

    @Test
    fun `last three months spans from start of two months ago to end of current`() {
        val (y, m) = Months.current()
        val (sy, sm) = Months.shift(y, m, -2)
        val r = DateRangeFilter.LastThreeMonths.resolve()
        assertEquals(Months.window(sy, sm).startMs, r.first)
        assertEquals(Months.window(y, m).endMs, r.second)
    }
}
