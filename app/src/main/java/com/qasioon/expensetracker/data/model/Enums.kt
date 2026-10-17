package com.qasioon.expensetracker.data.model

enum class AccountType(val label: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    CREDIT_CARD("Credit Card")
}

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class PeriodMode {
    MONTHLY,
    YEARLY
}
