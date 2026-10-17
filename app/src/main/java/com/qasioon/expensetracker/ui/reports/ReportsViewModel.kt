package com.qasioon.expensetracker.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.data.entity.TransactionEntity
import com.qasioon.expensetracker.data.model.AccountType
import com.qasioon.expensetracker.data.model.PeriodMode
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.model.BudgetStatus
import com.qasioon.expensetracker.ui.model.CategoryShare
import com.qasioon.expensetracker.ui.model.MonthBar
import com.qasioon.expensetracker.ui.model.TxnItem
import com.qasioon.expensetracker.util.Dates
import com.qasioon.expensetracker.util.Money
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

data class ReportsUiState(
    val mode: PeriodMode = PeriodMode.MONTHLY,
    val periodLabel: String = "",
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    val categoryShares: List<CategoryShare> = emptyList(),
    val monthBars: List<MonthBar> = emptyList(),
    val budgetStatus: List<BudgetStatus> = emptyList(),
    val budgetStatusMonth: String = "",
    val items: List<TxnItem> = emptyList(),
    val hasAnyData: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(private val app: ExpenseApp) : ViewModel() {

    private data class PeriodKey(val mode: PeriodMode, val yearMonth: YearMonth, val year: Int)

    private val _mode = MutableStateFlow(PeriodMode.MONTHLY)
    private val _yearMonth = MutableStateFlow(YearMonth.now())
    private val _year = MutableStateFlow(LocalDate.now().year)

    private val periodKey: Flow<PeriodKey> = combine(
        _mode, _yearMonth, _year
    ) { mode, yearMonth, year -> PeriodKey(mode, yearMonth, year) }.distinctUntilChanged()

    private val periodTransactions: Flow<List<TransactionEntity>> =
        periodKey.flatMapLatest { key ->
            app.transactionRepository.observeInRange(
                Dates.periodStart(key.mode, key.yearMonth, key.year),
                Dates.periodEndExclusive(key.mode, key.yearMonth, key.year)
            )
        }

    val uiState: StateFlow<ReportsUiState> = combine(
        periodKey,
        app.accountRepository.observeAll(),
        app.categoryRepository.observeAll(),
        periodTransactions,
        app.transactionRepository.observeAll()
    ) { key, accounts, categories, transactions, allTransactions ->
        val accountById = accounts.associateBy { it.id }
        val categoryById = categories.associateBy { it.id }

        val items = transactions.map { txn ->
            val account = accountById[txn.accountId]
            TxnItem(
                txn = txn,
                accountName = account?.name ?: "Unknown account",
                accountType = account?.type ?: AccountType.CASH,
                categoryName = txn.categoryId?.let { categoryById[it]?.name }
            )
        }

        val expenseTxns = transactions.filter { it.type == TransactionType.EXPENSE }
        val expenses = expenseTxns.sumOf { it.amount }
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val shares = if (expenses > 0) {
            expenseTxns
                .groupBy { it.categoryId }
                .map { (categoryId, list) ->
                    val amount = list.sumOf { it.amount }
                    CategoryShare(
                        name = categoryId?.let { categoryById[it]?.name } ?: "Uncategorized",
                        amount = amount,
                        fraction = (amount / expenses).toFloat()
                    )
                }
                .sortedByDescending { it.amount }
        } else {
            emptyList()
        }

        val now = YearMonth.now()
        val bars = (5 downTo 0).map { offset ->
            val month = now.minusMonths(offset.toLong())
            val range = Dates.monthRange(month)
            MonthBar(
                label = Dates.formatShortMonth(month),
                amount = allTransactions
                    .filter { it.type == TransactionType.EXPENSE && Dates.contains(range, it.date) }
                    .sumOf { it.amount }
            )
        }

        val currentMonthRange = Dates.monthRange(now)
        val budgets = categories
            .filter { it.monthlyBudget != null && it.monthlyBudget > 0 }
            .map { category ->
                val spent = allTransactions
                    .filter {
                        it.type == TransactionType.EXPENSE &&
                            it.categoryId == category.id &&
                            Dates.contains(currentMonthRange, it.date)
                    }
                    .sumOf { it.amount }
                BudgetStatus(category, spent)
            }
            .sortedByDescending { (it.category.monthlyBudget ?: 0.0) - it.spent }

        ReportsUiState(
            mode = key.mode,
            periodLabel = Dates.periodLabel(key.mode, key.yearMonth, key.year),
            income = income,
            expenses = expenses,
            categoryShares = shares,
            monthBars = bars,
            budgetStatus = budgets,
            budgetStatusMonth = Dates.formatMonth(now),
            items = items,
            hasAnyData = transactions.isNotEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun previousPeriod() {
        if (_mode.value == PeriodMode.MONTHLY) {
            _yearMonth.update { it.minusMonths(1) }
        } else {
            _year.update { it - 1 }
        }
    }

    fun nextPeriod() {
        if (_mode.value == PeriodMode.MONTHLY) {
            _yearMonth.update { minOf(it.plusMonths(1), YearMonth.now()) }
        } else {
            _year.update { minOf(it + 1, LocalDate.now().year) }
        }
    }

    fun setMode(mode: PeriodMode) {
        _mode.value = mode
    }

    companion object {
        fun factory(app: ExpenseApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { ReportsViewModel(app) }
        }
    }
}
