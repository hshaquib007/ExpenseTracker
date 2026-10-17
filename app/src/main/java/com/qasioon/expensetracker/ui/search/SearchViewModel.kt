package com.qasioon.expensetracker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.data.model.AccountType
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.model.TxnItem
import com.qasioon.expensetracker.util.Dates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SearchFilters(
    val query: String = "",
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val from: Long? = null,
    val to: Long? = null
) {
    val isActive: Boolean
        get() = query.isNotBlank() || type != null || categoryId != null ||
            accountId != null || from != null || to != null
}

class SearchViewModel(private val app: ExpenseApp) : ViewModel() {

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    val accounts = app.accountRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories = app.categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allTransactions = app.transactionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val results: StateFlow<List<TxnItem>> = combine(
        _filters,
        allTransactions,
        accounts,
        categories
    ) { filters, transactions, accountList, categoryList ->
        val accountById = accountList.associateBy { it.id }
        val categoryById = categoryList.associateBy { it.id }
        val toExclusive = filters.to?.let {
            Dates.millisOf(Dates.dateOf(it).plusDays(1))
        }

        transactions.asSequence()
            .filter { filters.type == null || it.type == filters.type }
            .filter { filters.accountId == null || it.accountId == filters.accountId }
            .filter { filters.categoryId == null || it.categoryId == filters.categoryId }
            .filter { filters.from == null || it.date >= filters.from }
            .filter { toExclusive == null || it.date < toExclusive }
            .filter { filter ->
                if (filters.query.isBlank()) return@filter true
                val q = filters.query.trim()
                val categoryName = filter.categoryId?.let { categoryById[it]?.name }.orEmpty()
                val accountName = accountById[filter.accountId]?.name.orEmpty()
                filter.note.contains(q, ignoreCase = true) ||
                    categoryName.contains(q, ignoreCase = true) ||
                    accountName.contains(q, ignoreCase = true) ||
                    filter.amount.toString().contains(q)
            }
            .map { txn ->
                val account = accountById[txn.accountId]
                TxnItem(
                    txn = txn,
                    accountName = account?.name ?: "Unknown account",
                    accountType = account?.type ?: AccountType.CASH,
                    categoryName = txn.categoryId?.let { categoryById[it]?.name }
                )
            }
            .toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(query: String) = _filters.update { it.copy(query = query) }

    fun setType(type: TransactionType?) = _filters.update { it.copy(type = type) }

    fun setCategory(id: Long?) = _filters.update {
        it.copy(categoryId = if (it.categoryId == id) null else id)
    }

    fun setAccount(id: Long?) = _filters.update {
        it.copy(accountId = if (it.accountId == id) null else id)
    }

    fun setFrom(millis: Long?) = _filters.update { it.copy(from = millis) }

    fun setTo(millis: Long?) = _filters.update { it.copy(to = millis) }

    fun clearAll() {
        _filters.value = SearchFilters(query = _filters.value.query)
    }

    companion object {
        fun factory(app: ExpenseApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { SearchViewModel(app) }
        }
    }
}
