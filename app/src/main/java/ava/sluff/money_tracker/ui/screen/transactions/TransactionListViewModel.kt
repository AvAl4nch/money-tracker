package ava.sluff.money_tracker.ui.screen.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ava.sluff.money_tracker.data.datastore.SettingsDataStore
import ava.sluff.money_tracker.data.local.entity.TransactionEntity
import ava.sluff.money_tracker.data.repository.CategoryRepository
import ava.sluff.money_tracker.data.repository.TransactionRepository
import ava.sluff.money_tracker.domain.model.Category
import ava.sluff.money_tracker.domain.model.Transaction
import ava.sluff.money_tracker.domain.model.TransactionType
import ava.sluff.money_tracker.domain.usecase.GetTransactionsUseCase
import ava.sluff.money_tracker.domain.usecase.UpdateTransactionCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    getTransactions: GetTransactionsUseCase,
    private val updateTransactionCategory: UpdateTransactionCategoryUseCase,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    settingsDataStore: SettingsDataStore
) : ViewModel() {

    val sortMode = MutableStateFlow(SortMode.NEWEST)
    val filterCategoryId = MutableStateFlow<Long?>(null)
    val searchQuery = MutableStateFlow("")
    val dateRange = MutableStateFlow<DateRangeFilter>(DateRangeFilter.All)

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currency: StateFlow<String> = settingsDataStore.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsDataStore.DEFAULT_CURRENCY)

    val transactions: StateFlow<List<Transaction>> =
        combine(getTransactions(), sortMode, filterCategoryId, searchQuery, dateRange) { list, sort, filter, query, range ->
            val q = query.trim()
            val filtered = list.asSequence()
                .filter { filter == null || it.categoryId == filter }
                .filter { range.contains(it.timestamp) }
                .filter {
                    q.isEmpty() ||
                        it.merchantName?.contains(q, ignoreCase = true) == true ||
                        it.description?.contains(q, ignoreCase = true) == true ||
                        it.note?.contains(q, ignoreCase = true) == true
                }
                .toList()
            when (sort) {
                SortMode.NEWEST -> filtered.sortedByDescending { it.timestamp }
                SortMode.OLDEST -> filtered.sortedBy { it.timestamp }
                SortMode.AMOUNT_HIGH -> filtered.sortedByDescending { it.amount }
                SortMode.AMOUNT_LOW -> filtered.sortedBy { it.amount }
                SortMode.CATEGORY -> filtered.sortedBy { it.categoryName ?: "￿" }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSortMode(mode: SortMode) {
        sortMode.value = mode
    }

    fun setFilterCategory(categoryId: Long?) {
        filterCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setDateRange(range: DateRangeFilter) {
        dateRange.value = range
    }

    fun updateCategory(transactionId: Long, categoryId: Long) {
        viewModelScope.launch { updateTransactionCategory(transactionId, categoryId) }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch { transactionRepository.delete(transactionId) }
    }

    fun addManualTransaction(
        amount: Double,
        type: TransactionType,
        merchant: String?,
        categoryId: Long?,
        note: String?,
        timestamp: Long
    ) {
        viewModelScope.launch {
            transactionRepository.insert(
                TransactionEntity(
                    amount = amount,
                    type = type.name,
                    merchantName = merchant?.takeIf { it.isNotBlank() },
                    description = null,
                    categoryId = categoryId,
                    rawSms = "",
                    smsSender = "MANUAL",
                    timestamp = timestamp,
                    balanceAfter = null,
                    isCategorizedByAi = false,
                    aiConfidence = null,
                    note = note?.takeIf { it.isNotBlank() }
                )
            )
        }
    }
}
