package com.qasioon.expensetracker.export

import android.content.Context
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.model.TxnItem
import com.qasioon.expensetracker.util.Dates
import com.qasioon.expensetracker.util.Money
import java.io.File

object CsvExporter {

    fun export(context: Context, items: List<TxnItem>, periodLabel: String): File {
        val file = File(
            ShareUtil.exportDir(context),
            "report_${System.currentTimeMillis()}.csv"
        )
        val sb = StringBuilder()
        sb.appendLine("# Expense Tracker report - $periodLabel")
        sb.appendLine("Date,Type,Category,Account,Note,Amount")
        items.forEach { item ->
            val type = if (item.txn.type == TransactionType.EXPENSE) "Expense" else "Income"
            sb.appendLine(
                listOf(
                    Dates.formatDay(item.txn.date),
                    type,
                    item.categoryName.orEmpty(),
                    item.accountName,
                    item.txn.note,
                    item.txn.amount.toString()
                ).joinToString(",") { escape(it) }
            )
        }
        val expenses = items.filter { it.txn.type == TransactionType.EXPENSE }.sumOf { it.txn.amount }
        val income = items.filter { it.txn.type == TransactionType.INCOME }.sumOf { it.txn.amount }
        sb.appendLine()
        sb.appendLine("Total Income,,,$income")
        sb.appendLine("Total Expenses,,,$expenses")
        file.writeText(sb.toString())
        return file
    }

    private fun escape(value: String): String =
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    fun totalLabel(items: List<TxnItem>): String {
        val expenses = items.filter { it.txn.type == TransactionType.EXPENSE }.sumOf { it.txn.amount }
        return Money.format(expenses)
    }
}
