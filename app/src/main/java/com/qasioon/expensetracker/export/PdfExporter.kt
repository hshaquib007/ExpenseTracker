package com.qasioon.expensetracker.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.qasioon.expensetracker.data.model.TransactionType
import com.qasioon.expensetracker.ui.model.TxnItem
import com.qasioon.expensetracker.util.Dates
import com.qasioon.expensetracker.util.Money
import java.io.File

object PdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 18f

    fun export(
        context: Context,
        items: List<TxnItem>,
        periodLabel: String,
        income: Double,
        expenses: Double
    ): File {
        val file = File(
            ShareUtil.exportDir(context),
            "report_${System.currentTimeMillis()}.pdf"
        )

        val document = PdfDocument()

        val titlePaint = Paint().apply {
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
            isAntiAlias = true
        }
        val rowPaint = Paint().apply {
            textSize = 10f
            color = Color.DKGRAY
            isAntiAlias = true
        }

        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas = page.canvas
        var y = MARGIN + 20f

        fun newPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            canvas = page.canvas
            y = MARGIN + 20f
        }

        canvas.drawText("Expense Tracker Report", MARGIN, y, titlePaint)
        y += LINE_HEIGHT
        canvas.drawText(periodLabel, MARGIN, y, rowPaint)
        y += LINE_HEIGHT + 8f

        canvas.drawText("Date", MARGIN, y, headerPaint)
        canvas.drawText("Type", MARGIN + 90f, y, headerPaint)
        canvas.drawText("Category", MARGIN + 150f, y, headerPaint)
        canvas.drawText("Account", MARGIN + 270f, y, headerPaint)
        canvas.drawText("Amount", PAGE_WIDTH - MARGIN - 60f, y, headerPaint)
        y += 6f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, headerPaint)
        y += LINE_HEIGHT

        items.forEach { item ->
            if (y > PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 2) {
                newPage()
            }
            val type = if (item.txn.type == TransactionType.EXPENSE) "Exp" else "Inc"
            val date = Dates.formatShortDay(item.txn.date)
            canvas.drawText(date, MARGIN, y, rowPaint)
            canvas.drawText(type, MARGIN + 90f, y, rowPaint)
            canvas.drawText(clip(item.categoryName ?: "-", 18f), MARGIN + 150f, y, rowPaint)
            canvas.drawText(clip(item.accountName, 20f), MARGIN + 270f, y, rowPaint)
            val sign = if (item.txn.type == TransactionType.EXPENSE) "-" else "+"
            canvas.drawText(
                sign + Money.format(item.txn.amount),
                PAGE_WIDTH - MARGIN - 60f,
                y,
                rowPaint
            )
            y += LINE_HEIGHT
        }

        if (y > PAGE_HEIGHT - MARGIN - LINE_HEIGHT * 4) {
            newPage()
        }
        y += 8f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, headerPaint)
        y += LINE_HEIGHT + 4f
        canvas.drawText("Total Income:   ${Money.format(income)}", MARGIN, y, headerPaint)
        y += LINE_HEIGHT
        canvas.drawText("Total Expenses: ${Money.format(expenses)}", MARGIN, y, headerPaint)
        y += LINE_HEIGHT
        canvas.drawText(
            "Balance: ${Money.format(income - expenses)}",
            MARGIN,
            y,
            headerPaint
        )

        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun clip(text: String, widthInChars: Float): String {
        val max = widthInChars.toInt()
        return if (text.length > max) text.take(max - 1) + "…" else text
    }
}
