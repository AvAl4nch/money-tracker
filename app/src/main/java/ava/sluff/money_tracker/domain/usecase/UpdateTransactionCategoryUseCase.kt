package ava.sluff.money_tracker.domain.usecase

import ava.sluff.money_tracker.data.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionCategoryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: Long, categoryId: Long) =
        transactionRepository.updateCategory(transactionId, categoryId)
}
