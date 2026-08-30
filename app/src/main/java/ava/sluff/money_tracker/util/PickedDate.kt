package ava.sluff.money_tracker.util

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object PickedDate {

    /**
     * Moves [original] to the calendar day the user picked, keeping its time of day.
     *
     * Material's date picker reports the selection as UTC midnight. Storing that value
     * directly would both discard the transaction's time and, in zones behind UTC, land it on
     * the previous local day — which can move a transaction into the wrong month.
     */
    fun applyTo(
        original: Long,
        pickedUtcMidnight: Long,
        zone: TimeZone = TimeZone.getDefault()
    ): Long {
        val picked = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US).apply {
            timeInMillis = pickedUtcMidnight
        }
        return Calendar.getInstance(zone, Locale.US).apply {
            timeInMillis = original
            set(Calendar.YEAR, picked.get(Calendar.YEAR))
            set(Calendar.MONTH, picked.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }
}
