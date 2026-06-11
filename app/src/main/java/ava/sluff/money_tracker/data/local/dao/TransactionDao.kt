package ava.sluff.money_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ava.sluff.money_tracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query(
        """SELECT t.*, c.name AS category_name, c.icon AS category_icon, c.color AS category_color
           FROM transactions t LEFT JOIN categories c ON t.category_id = c.id
           ORDER BY t.timestamp DESC"""
    )
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>>

    @Query(
        """SELECT t.*, c.name AS category_name, c.icon AS category_icon, c.color AS category_color
           FROM transactions t LEFT JOIN categories c ON t.category_id = c.id
           WHERE t.timestamp BETWEEN :startTime AND :endTime
           ORDER BY t.timestamp DESC"""
    )
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionWithCategory>>

    @Query(
        """SELECT COALESCE(c.name, 'Uncategorized') AS name, SUM(t.amount) AS total
           FROM transactions t LEFT JOIN categories c ON t.category_id = c.id
           WHERE t.type = 'DEBIT' AND t.timestamp BETWEEN :startTime AND :endTime
           GROUP BY COALESCE(c.name, 'Uncategorized') ORDER BY total DESC"""
    )
    fun getSpendingByCategory(startTime: Long, endTime: Long): Flow<List<CategorySpending>>

    @Query(
        """SELECT strftime('%Y-%m', t.timestamp / 1000, 'unixepoch', 'localtime') AS ym,
                  SUM(t.amount) AS total
           FROM transactions t
           WHERE t.type = 'DEBIT' AND t.timestamp BETWEEN :startTime AND :endTime
           GROUP BY ym ORDER BY ym ASC"""
    )
    fun getMonthlyTotals(startTime: Long, endTime: Long): Flow<List<MonthlyTotal>>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET category_id = :categoryId, is_categorized_by_ai = 0 WHERE id = :transactionId")
    suspend fun updateCategory(transactionId: Long, categoryId: Long)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE raw_sms = :rawSms")
    suspend fun countByRawSms(rawSms: String): Int
}
