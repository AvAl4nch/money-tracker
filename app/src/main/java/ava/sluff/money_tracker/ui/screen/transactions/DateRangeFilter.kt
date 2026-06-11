package ava.sluff.money_tracker.ui.screen.transactions

import ava.sluff.money_tracker.util.Months

sealed class DateRangeFilter(val label: String) {

    abstract fun resolve(): Pair<Long, Long>

    fun contains(timestamp: Long): Boolean {
        val (start, end) = resolve()
        return timestamp in start..end
    }

    data object All : DateRangeFilter("All") {
        override fun resolve() = 0L to Long.MAX_VALUE
    }

    data object ThisMonth : DateRangeFilter("This month") {
        override fun resolve() = Months.current().let { (y, m) ->
            Months.window(y, m).let { it.startMs to it.endMs }
        }
    }

    data object LastMonth : DateRangeFilter("Last month") {
        override fun resolve() = Months.current().let { (y, m) ->
            val (py, pm) = Months.shift(y, m, -1)
            Months.window(py, pm).let { it.startMs to it.endMs }
        }
    }

    data object LastThreeMonths : DateRangeFilter("Last 3 months") {
        override fun resolve(): Pair<Long, Long> {
            val (y, m) = Months.current()
            val (sy, sm) = Months.shift(y, m, -2)
            return Months.window(sy, sm).startMs to Months.window(y, m).endMs
        }
    }

    data class Custom(val startMs: Long, val endMs: Long) : DateRangeFilter("Custom") {
        override fun resolve() = startMs to endMs
    }

    companion object {
        // Lazy so the nested `data object` singletons are fully initialized before
        // this list captures them. A direct `listOf(...)` runs during the companion
        // object's <clinit>, which can execute before the nested objects' INSTANCE
        // fields are set, capturing nulls and causing an NPE at `preset::class`.
        val presets: List<DateRangeFilter> by lazy {
            listOf(All, ThisMonth, LastMonth, LastThreeMonths)
        }
    }
}
