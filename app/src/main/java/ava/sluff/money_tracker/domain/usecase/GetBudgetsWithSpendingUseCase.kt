package ava.sluff.money_tracker.domain.usecase

import ava.sluff.money_tracker.data.local.dao.BudgetWithSpending
import ava.sluff.money_tracker.data.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBudgetsWithSpendingUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(startTime: Long, endTime: Long): Flow<List<BudgetWithSpending>> =
        budgetRepository.getBudgetsWithSpending(startTime, endTime)
}
