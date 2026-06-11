package ava.sluff.money_tracker.domain.model

data class CategorizationResult(
    val categoryName: String,
    val merchantName: String?,
    val description: String?,
    val confidence: Float,
    val type: TransactionType,
    val amount: Double,
    val balanceAfter: Double?
)
