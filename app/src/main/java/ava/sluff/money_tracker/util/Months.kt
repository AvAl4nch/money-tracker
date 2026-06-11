package ava.sluff.money_tracker.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthWindow(val year: Int, val month: Int, val startMs: Long, val endMs: Long)

object Months {

    fun window(year: Int, month: Int): MonthWindow {
        val cal = Calendar.getInstance().apply {
            clear()
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMs = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val endMs = cal.timeInMillis - 1
        return MonthWindow(year, month, startMs, endMs)
    }

    fun shift(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        val cal = Calendar.getInstance().apply {
            clear(); set(year, month, 1)
            add(Calendar.MONTH, delta)
        }
        return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }

    fun label(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply { clear(); set(year, month, 1) }
        return SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date(cal.timeInMillis))
    }

    fun shortLabel(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply { clear(); set(year, month, 1) }
        return SimpleDateFormat("MMM", Locale.ENGLISH).format(Date(cal.timeInMillis))
    }

    /** n windows ending at (year, month), oldest first. */
    fun lastMonths(year: Int, month: Int, n: Int): List<MonthWindow> =
        (n - 1 downTo 0).map { back ->
            val (y, m) = shift(year, month, -back)
            window(y, m)
        }

    fun current(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }
}
