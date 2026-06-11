package ava.sluff.money_tracker.domain.usecase

import ava.sluff.money_tracker.data.local.dao.CategorySpending
import ava.sluff.money_tracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSpendingSummaryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(startTime: Long, endTime: Long): Flow<List<CategorySpending>> =
        transactionRepository.getSpendingByCategory(startTime, endTime)
}
