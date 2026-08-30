package ava.sluff.money_tracker.domain.usecase

import ava.sluff.money_tracker.data.repository.TransactionRepository
import ava.sluff.money_tracker.domain.model.Transaction
import ava.sluff.money_tracker.domain.model.TransactionEdits
import ava.sluff.money_tracker.domain.model.applyEdits
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(original: Transaction, edits: TransactionEdits) =
        transactionRepository.update(original.applyEdits(edits))
}
