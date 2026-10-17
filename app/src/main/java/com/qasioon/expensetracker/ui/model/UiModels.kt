package com.qasioon.expensetracker.ui.model

import com.qasioon.expensetracker.data.entity.AccountEntity
import com.qasioon.expensetracker.data.entity.CategoryEntity
import com.qasioon.expensetracker.data.entity.TransactionEntity
import com.qasioon.expensetracker.data.model.AccountType

data class TxnItem(
    val txn: TransactionEntity,
    val accountName: String,
    val accountType: AccountType,
    val categoryName: String?
)

data class AccountSummary(
    val account: AccountEntity,
    val spent: Double
)

data class CategoryShare(
    val name: String,
    val amount: Double,
    val fraction: Float
)

data class MonthBar(
    val label: String,
    val amount: Double
)

data class BudgetStatus(
    val category: CategoryEntity,
    val spent: Double
)
