package ava.sluff.money_tracker.domain.model

import ava.sluff.money_tracker.data.local.entity.TransactionEntity

/** The fields a user is allowed to correct on an existing transaction. */
data class TransactionEdits(
    val amount: Double,
    val type: TransactionType,
    val merchantName: String?,
    val description: String?,
    val categoryId: Long?,
    val note: String?,
    val timestamp: Long
)

/**
 * Merges user corrections into an existing transaction.
 *
 * The original SMS and its sender are never overwritten — they stay the source of truth the
 * edit can always be checked against. Editing clears [TransactionEntity.isCategorizedByAi]
 * so a hand-fixed row stops presenting itself as an AI decision, while the recorded
 * confidence is kept as history.
 */
fun Transaction.applyEdits(edits: TransactionEdits): TransactionEntity = TransactionEntity(
    id = id,
    amount = edits.amount,
    type = edits.type.name,
    merchantName = edits.merchantName?.takeIf { it.isNotBlank() },
    description = edits.description?.takeIf { it.isNotBlank() },
    categoryId = edits.categoryId,
    rawSms = rawSms,
    smsSender = smsSender,
    timestamp = edits.timestamp,
    balanceAfter = balanceAfter,
    isCategorizedByAi = false,
    aiConfidence = aiConfidence,
    note = edits.note?.takeIf { it.isNotBlank() }
)
