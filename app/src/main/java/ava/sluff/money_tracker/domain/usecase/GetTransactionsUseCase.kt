package ava.sluff.money_tracker.domain.usecase

import ava.sluff.money_tracker.data.repository.TransactionRepository
import ava.sluff.money_tracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> = transactionRepository.getAllTransactions()
}
