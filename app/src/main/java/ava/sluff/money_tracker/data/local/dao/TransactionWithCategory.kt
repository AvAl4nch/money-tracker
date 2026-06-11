package ava.sluff.money_tracker.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Embedded
import ava.sluff.money_tracker.data.local.entity.TransactionEntity

data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_icon") val categoryIcon: String?,
    @ColumnInfo(name = "category_color") val categoryColor: Long?
)
