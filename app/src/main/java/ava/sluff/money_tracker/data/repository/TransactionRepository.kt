package ava.sluff.money_tracker.data.repository

import ava.sluff.money_tracker.data.local.dao.CategorySpending
import ava.sluff.money_tracker.data.local.dao.MonthlyTotal
import ava.sluff.money_tracker.data.local.dao.TransactionDao
import ava.sluff.money_tracker.data.local.dao.TransactionWithCategory
import ava.sluff.money_tracker.data.local.entity.TransactionEntity
import ava.sluff.money_tracker.domain.model.Transaction
import ava.sluff.money_tracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactionsWithCategory().map { list -> list.map { it.toDomain() } }

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetween(startTime, endTime).map { list -> list.map { it.toDomain() } }

    fun getSpendingByCategory(startTime: Long, endTime: Long): Flow<List<CategorySpending>> =
        transactionDao.getSpendingByCategory(startTime, endTime)

    fun getMonthlyTotals(startTime: Long, endTime: Long): Flow<List<MonthlyTotal>> =
        transactionDao.getMonthlyTotals(startTime, endTime)

    suspend fun insert(entity: TransactionEntity): Long = transactionDao.insert(entity)

    suspend fun update(entity: TransactionEntity) = transactionDao.update(entity)

    suspend fun updateCategory(transactionId: Long, categoryId: Long) =
        transactionDao.updateCategory(transactionId, categoryId)

    suspend fun delete(transactionId: Long) = transactionDao.deleteById(transactionId)

    suspend fun isDuplicate(rawSms: String): Boolean = transactionDao.countByRawSms(rawSms) > 0
}

fun TransactionWithCategory.toDomain(): Transaction = Transaction(
    id = transaction.id,
    amount = transaction.amount,
    type = if (transaction.type == "CREDIT") TransactionType.CREDIT else TransactionType.DEBIT,
    merchantName = transaction.merchantName,
    description = transaction.description,
    categoryId = transaction.categoryId,
    categoryName = categoryName,
    categoryIcon = categoryIcon,
    categoryColor = categoryColor,
    rawSms = transaction.rawSms,
    smsSender = transaction.smsSender,
    timestamp = transaction.timestamp,
    balanceAfter = transaction.balanceAfter,
    isCategorizedByAi = transaction.isCategorizedByAi,
    aiConfidence = transaction.aiConfidence,
    note = transaction.note
)
