package com.qasioon.expensetracker.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.data.entity.AccountEntity
import com.qasioon.expensetracker.data.entity.CategoryEntity
import com.qasioon.expensetracker.data.entity.TransactionEntity
import com.qasioon.expensetracker.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.qasioon.expensetracker.util.Dates

data class AddEditState(
    val loaded: Boolean = false,
    val editingId: Long = -1L,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val dateMillis: Long = Dates.millisOf(java.time.LocalDate.now()),
    val note: String = "",
    val categoryId: Long? = null,
    val accountId: Long = -1L,
    val error: String? = null,
    val closed: Boolean = false
)

class AddEditViewModel(
    private val app: ExpenseApp,
    private val transactionId: Long,
    private val initialType: TransactionType,
    private val initialAccountId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditState())
    val state = _state.asStateFlow()

    val accounts = app.accountRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories = app.categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun load() {
        if (_state.value.loaded) return
        viewModelScope.launch {
            val existing = if (transactionId > 0) {
                app.transactionRepository.getById(transactionId)
            } else {
                null
            }
            _state.update { current ->
                if (existing != null) {
                    current.copy(
                        loaded = true,
                        editingId = existing.id,
                        type = existing.type,
                        amountText = formatAmount(existing.amount),
                        dateMillis = existing.date,
                        note = existing.note,
                        categoryId = existing.categoryId,
                        accountId = existing.accountId
                    )
                } else {
                    current.copy(
                        loaded = true,
                        type = initialType,
                        accountId = initialAccountId
                    )
                }
            }
        }
    }

    fun ensureDefaultAccount() {
        val current = _state.value
        if (current.loaded && current.accountId <= 0L && accounts.value.isNotEmpty()) {
            _state.update { it.copy(accountId = accounts.value.first().id) }
        }
    }

    fun setType(type: TransactionType) = _state.update { it.copy(type = type, error = null) }

    fun setAmount(text: String) {
        val filtered = if (text.all { it.isDigit() || it == '.' } && text.count { it == '.' } <= 1) {
            text
        } else {
            _state.value.amountText
        }
        _state.update { it.copy(amountText = filtered, error = null) }
    }

    fun setDate(millis: Long) = _state.update { it.copy(dateMillis = millis, error = null) }

    fun setNote(note: String) = _state.update { it.copy(note = note) }

    fun setCategory(id: Long?) = _state.update { it.copy(categoryId = id, error = null) }

    fun setAccount(id: Long) = _state.update { it.copy(accountId = id, error = null) }

    fun save() {
        val s = _state.value
        val amount = s.amountText.toDoubleOrNull()
        when {
            amount == null || amount <= 0.0 ->
                _state.update { it.copy(error = "Enter a valid amount") }

            s.accountId <= 0L ->
                _state.update { it.copy(error = "Add an account first (Accounts tab)") }

            s.type == TransactionType.EXPENSE && s.categoryId == null ->
                _state.update { it.copy(error = "Select a category") }

            else -> {
                viewModelScope.launch {
                    val txn = TransactionEntity(
                        id = if (s.editingId > 0) s.editingId else 0L,
                        type = s.type,
                        amount = amount,
                        date = s.dateMillis,
                        note = s.note.trim(),
                        accountId = s.accountId,
                        categoryId = if (s.type == TransactionType.INCOME) null else s.categoryId
                    )
                    if (s.editingId > 0) {
                        app.transactionRepository.update(txn)
                    } else {
                        app.transactionRepository.insert(txn)
                    }
                    _state.update { it.copy(closed = true) }
                }
            }
        }
    }

    fun delete() {
        val id = _state.value.editingId
        if (id <= 0) return
        viewModelScope.launch {
            val txn = app.transactionRepository.getById(id) ?: return@launch
            app.transactionRepository.delete(txn)
            _state.update { it.copy(closed = true) }
        }
    }

    private fun formatAmount(amount: Double): String =
        if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()

    companion object {
        fun factory(
            app: ExpenseApp,
            transactionId: Long,
            initialType: TransactionType,
            initialAccountId: Long
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddEditViewModel(app, transactionId, initialType, initialAccountId)
            }
        }
    }
}
