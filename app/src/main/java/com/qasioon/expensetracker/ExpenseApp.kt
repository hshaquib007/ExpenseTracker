package com.qasioon.expensetracker

import android.app.Application
import com.qasioon.expensetracker.data.db.AppDatabase
import com.qasioon.expensetracker.data.entity.AccountEntity
import com.qasioon.expensetracker.data.entity.CategoryEntity
import com.qasioon.expensetracker.data.model.AccountType
import com.qasioon.expensetracker.data.prefs.BudgetPrefs
import com.qasioon.expensetracker.data.repo.AccountRepository
import com.qasioon.expensetracker.data.repo.CategoryRepository
import com.qasioon.expensetracker.data.repo.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExpenseApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.build(this) }
    val accountRepository: AccountRepository by lazy { AccountRepository(database.accountDao()) }
    val categoryRepository: CategoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val transactionRepository: TransactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val budgetPrefs: BudgetPrefs by lazy { BudgetPrefs(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { seedIfEmpty() }
    }

    private suspend fun seedIfEmpty() {
        if (accountRepository.count() == 0) {
            accountRepository.insert(AccountEntity(name = "Cash", type = AccountType.CASH))
        }
        if (categoryRepository.count() == 0) {
            listOf(
                "Food", "Grocery", "Entertainment", "Transport",
                "Medical", "Bills", "Shopping", "Other"
            ).forEach { name ->
                categoryRepository.insert(CategoryEntity(name = name))
            }
        }
    }
}
