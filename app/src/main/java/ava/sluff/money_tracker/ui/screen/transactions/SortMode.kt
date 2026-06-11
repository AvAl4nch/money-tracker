package ava.sluff.money_tracker.ui.screen.transactions

enum class SortMode(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    AMOUNT_HIGH("Highest amount"),
    AMOUNT_LOW("Lowest amount"),
    CATEGORY("By category")
}
