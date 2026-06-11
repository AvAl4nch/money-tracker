package ava.sluff.money_tracker.ui.screen.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ava.sluff.money_tracker.data.datastore.SettingsDataStore
import ava.sluff.money_tracker.data.local.dao.BudgetWithSpending
import ava.sluff.money_tracker.data.local.dao.CategorySpending
import ava.sluff.money_tracker.data.repository.CategoryRepository
import ava.sluff.money_tracker.data.repository.TransactionRepository
import ava.sluff.money_tracker.domain.model.Category
import ava.sluff.money_tracker.domain.usecase.GetBudgetsWithSpendingUseCase
import ava.sluff.money_tracker.domain.usecase.GetSpendingSummaryUseCase
import ava.sluff.money_tracker.domain.usecase.SetBudgetUseCase
import ava.sluff.money_tracker.util.MonthWindow
import ava.sluff.money_tracker.util.Months
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonthBar(val year: Int, val month: Int, val label: String, val total: Double)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SpendingSummaryViewModel @Inject constructor(
    private val getSpendingSummary: GetSpendingSummaryUseCase,
    private val getBudgetsWithSpending: GetBudgetsWithSpendingUseCase,
    private val setBudgetUseCase: SetBudgetUseCase,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    settingsDataStore: SettingsDataStore
) : ViewModel() {

    val selectedMonth = MutableStateFlow(Months.current())

    val monthLabel: StateFlow<String> = selectedMonth
        .map { (y, m) -> Months.label(y, m) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val window: StateFlow<MonthWindow> = selectedMonth
        .map { (y, m) -> Months.window(y, m) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Months.current().let { (y, m) -> Months.window(y, m) })

    val spending: StateFlow<List<CategorySpending>> = window
        .flatMapLatest { w -> getSpendingSummary(w.startMs, w.endMs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalSpent: StateFlow<Double> = spending
        .map { list -> list.sumOf { it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val budgets: StateFlow<List<BudgetWithSpending>> = window
        .flatMapLatest { w -> getBudgetsWithSpending(w.startMs, w.endMs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currency: StateFlow<String> = settingsDataStore.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsDataStore.DEFAULT_CURRENCY)

    /** Last 6 calendar months ending at the CURRENT month (fixed axis; selection moves the highlight). */
    val trend: StateFlow<List<MonthBar>> = run {
        val (cy, cm) = Months.current()
        val windows = Months.lastMonths(cy, cm, 6)
        transactionRepository.getMonthlyTotals(windows.first().startMs, windows.last().endMs)
            .map { totals ->
                val byYm = totals.associateBy { it.ym }
                windows.map { w ->
                    val ym = String.format(java.util.Locale.US, "%04d-%02d", w.year, w.month + 1)
                    MonthBar(w.year, w.month, Months.shortLabel(w.year, w.month), byYm[ym]?.total ?: 0.0)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.let { (y, m) -> Months.shift(y, m, -1) }
    }

    fun nextMonth() {
        selectedMonth.value = selectedMonth.value.let { (y, m) -> Months.shift(y, m, +1) }
    }

    fun selectMonth(year: Int, month: Int) {
        selectedMonth.value = year to month
    }

    fun setBudget(categoryId: Long, monthlyLimit: Double) {
        viewModelScope.launch { setBudgetUseCase(categoryId, monthlyLimit) }
    }
}
