package ava.sluff.money_tracker.domain.model

data class Transaction(
    val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val merchantName: String? = null,
    val description: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: Long? = null,
    val rawSms: String,
    val smsSender: String,
    val timestamp: Long,
    val balanceAfter: Double? = null,
    val isCategorizedByAi: Boolean = false,
    val aiConfidence: Float? = null,
    val note: String? = null
)
