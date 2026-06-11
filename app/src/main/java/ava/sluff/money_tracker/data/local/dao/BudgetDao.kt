package ava.sluff.money_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ava.sluff.money_tracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query(
        """SELECT b.id AS budget_id, b.category_id AS category_id, c.name AS category_name,
                  c.color AS category_color, b.monthly_limit AS monthly_limit,
                  COALESCE((SELECT SUM(t.amount) FROM transactions t
                            WHERE t.category_id = b.category_id AND t.type = 'DEBIT'
                              AND t.timestamp BETWEEN :startTime AND :endTime), 0) AS spent
           FROM budgets b JOIN categories c ON c.id = b.category_id
           ORDER BY c.sort_order ASC"""
    )
    fun getBudgetsWithSpending(startTime: Long, endTime: Long): Flow<List<BudgetWithSpending>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE category_id = :categoryId")
    suspend fun deleteByCategoryId(categoryId: Long)

    @Query("SELECT * FROM budgets WHERE category_id = :categoryId")
    suspend fun getByCategoryId(categoryId: Long): BudgetEntity?
}
