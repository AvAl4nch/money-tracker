package ava.sluff.money_tracker.data.repository

import ava.sluff.money_tracker.data.local.dao.BudgetDao
import ava.sluff.money_tracker.data.local.dao.BudgetWithSpending
import ava.sluff.money_tracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {

    fun getBudgetsWithSpending(startTime: Long, endTime: Long): Flow<List<BudgetWithSpending>> =
        budgetDao.getBudgetsWithSpending(startTime, endTime)

    /** limit <= 0 removes the budget for that category. */
    suspend fun setBudget(categoryId: Long, monthlyLimit: Double) {
        if (monthlyLimit > 0) {
            val existing = budgetDao.getByCategoryId(categoryId)
            budgetDao.upsert(
                BudgetEntity(id = existing?.id ?: 0L, categoryId = categoryId, monthlyLimit = monthlyLimit)
            )
        } else {
            budgetDao.deleteByCategoryId(categoryId)
        }
    }
}
