package ava.sluff.money_tracker.domain.usecase

import ava.sluff.money_tracker.data.repository.BudgetRepository
import javax.inject.Inject

class SetBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(categoryId: Long, monthlyLimit: Double) =
        budgetRepository.setBudget(categoryId, monthlyLimit)
}
