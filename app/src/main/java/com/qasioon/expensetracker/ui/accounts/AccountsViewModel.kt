package com.qasioon.expensetracker.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qasioon.expensetracker.ExpenseApp
import com.qasioon.expensetracker.data.entity.AccountEntity
import com.qasioon.expensetracker.data.model.AccountType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(private val app: ExpenseApp) : ViewModel() {

    val accounts = app.accountRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(
        name: String,
        type: AccountType,
        creditLimit: Double?,
        billDueDay: Int?
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            app.accountRepository.insert(
                AccountEntity(
                    name = trimmed,
                    type = type,
                    creditLimit = if (type == AccountType.CREDIT_CARD) creditLimit else null,
                    billDueDay = if (type == AccountType.CREDIT_CARD) billDueDay else null
                )
            )
        }
    }

    fun update(
        id: Long,
        name: String,
        type: AccountType,
        creditLimit: Double?,
        billDueDay: Int?
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val existing = app.accountRepository.getById(id) ?: return@launch
            app.accountRepository.update(
                existing.copy(
                    name = trimmed,
                    type = type,
                    creditLimit = if (type == AccountType.CREDIT_CARD) creditLimit else null,
                    billDueDay = if (type == AccountType.CREDIT_CARD) billDueDay else null
                )
            )
        }
    }

    fun delete(account: AccountEntity) {
        viewModelScope.launch {
            app.accountRepository.delete(account)
        }
    }

    companion object {
        fun factory(app: ExpenseApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { AccountsViewModel(app) }
        }
    }
}

class AccountDetailViewModel(
    private val app: ExpenseApp,
    private val accountId: Long
) : ViewModel() {

    val account = app.accountRepository.observeById(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions = app.transactionRepository.observeForAccount(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun factory(app: ExpenseApp, accountId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { AccountDetailViewModel(app, accountId) }
            }
    }
}
