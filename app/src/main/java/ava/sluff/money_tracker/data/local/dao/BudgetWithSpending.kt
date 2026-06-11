package ava.sluff.money_tracker.data.local.dao

import androidx.room.ColumnInfo

data class BudgetWithSpending(
    @ColumnInfo(name = "budget_id") val budgetId: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "category_name") val categoryName: String,
    @ColumnInfo(name = "category_color") val categoryColor: Long,
    @ColumnInfo(name = "monthly_limit") val monthlyLimit: Double,
    val spent: Double
)
