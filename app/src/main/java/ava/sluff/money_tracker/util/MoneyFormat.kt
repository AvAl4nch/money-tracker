package ava.sluff.money_tracker.util

import java.util.Locale

object MoneyFormat {
    /** Locale-stable money rendering: grouping commas, 3 decimals (JOD convention). */
    fun amount(value: Double): String = String.format(Locale.US, "%,.3f", value)
}
