package com.qasioon.expensetracker.data.prefs

import android.content.Context

class BudgetPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)

    fun getMonthlyBudget(): Double =
        prefs.getFloat(KEY_MONTHLY_BUDGET, -1f).let { if (it < 0f) 0.0 else it.toDouble() }

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, amount.toFloat()).apply()
    }

    private companion object {
        const val KEY_MONTHLY_BUDGET = "monthly_budget"
    }
}
