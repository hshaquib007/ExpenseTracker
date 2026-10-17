package com.qasioon.expensetracker.ui.home

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
import com.qasioon.expensetracker.ui.model.AccountSummary
import com.qasioon.expensetracker.ui.model.TxnItem
import com.qasioon.expensetracker.util.Dates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

data class HomeUiState(
    val mode: PeriodMode = PeriodMode.MONTHLY,
    val periodLabel: String = "",
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    val balance: Double = 0.0,
    val budget: Double = 0.0,
    val items: List<TxnItem> = emptyList(),
    val accounts: List<AccountSummary> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val app: ExpenseApp) : ViewModel() {

    private data class PeriodKey(val mode: PeriodMode, val yearMonth: YearMonth, val year: Int)

    private val _mode = MutableStateFlow(PeriodMode.MONTHLY)
    private val _yearMonth = MutableStateFlow(YearMonth.now())
    private val _year = MutableStateFlow(LocalDate.now().year)
    private val _budget = MutableStateFlow(app.budgetPrefs.getMonthlyBudget())

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

    val uiState: StateFlow<HomeUiState> = combine(
        periodKey,
        app.accountRepository.observeAll(),
        app.categoryRepository.observeAll(),
        periodTransactions,
        _budget
    ) { key, accounts, categories, transactions, budget ->
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

        val expenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val spentByAccount = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.accountId }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        HomeUiState(
            mode = key.mode,
            periodLabel = Dates.periodLabel(key.mode, key.yearMonth, key.year),
            income = income,
            expenses = expenses,
            balance = income - expenses,
            budget = budget,
            items = items,
            accounts = accounts.map { account ->
                AccountSummary(account, spentByAccount[account.id] ?: 0.0)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

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

    fun setBudget(amount: Double) {
        app.budgetPrefs.setMonthlyBudget(amount)
        _budget.value = amount
    }

    companion object {
        fun factory(app: ExpenseApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(app) }
        }
    }
}
