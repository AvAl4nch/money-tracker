package ava.sluff.money_tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id"), Index("timestamp")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val type: String,
    @ColumnInfo(name = "merchant_name") val merchantName: String?,
    val description: String?,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "raw_sms") val rawSms: String,
    @ColumnInfo(name = "sms_sender") val smsSender: String,
    val timestamp: Long,
    @ColumnInfo(name = "balance_after") val balanceAfter: Double?,
    @ColumnInfo(name = "is_categorized_by_ai") val isCategorizedByAi: Boolean,
    @ColumnInfo(name = "ai_confidence") val aiConfidence: Float?,
    val note: String?
)
