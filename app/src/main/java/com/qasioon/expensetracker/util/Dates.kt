package com.qasioon.expensetracker.util

import com.qasioon.expensetracker.data.model.PeriodMode
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object Money {
    private val formatter: NumberFormat =
        NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
        }

    fun format(amount: Double): String = "₹" + formatter.format(amount)
}

object Dates {

    private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val shortDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM")
    private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val shortMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yy")

    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun millisOf(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun dateOf(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun formatDay(millis: Long): String = dayFormatter.format(dateOf(millis))

    fun formatShortDay(millis: Long): String = shortDayFormatter.format(dateOf(millis))

    fun formatMonth(yearMonth: YearMonth): String = monthFormatter.format(yearMonth)

    fun formatShortMonth(yearMonth: YearMonth): String = shortMonthFormatter.format(yearMonth)

    fun periodStart(mode: PeriodMode, yearMonth: YearMonth, year: Int): Long = when (mode) {
        PeriodMode.MONTHLY -> millisOf(yearMonth.atDay(1))
        PeriodMode.YEARLY -> millisOf(YearMonth.of(year, 1).atDay(1))
    }

    fun periodEndExclusive(mode: PeriodMode, yearMonth: YearMonth, year: Int): Long = when (mode) {
        PeriodMode.MONTHLY -> millisOf(yearMonth.plusMonths(1).atDay(1))
        PeriodMode.YEARLY -> millisOf(YearMonth.of(year, 12).plusMonths(1).atDay(1))
    }

    fun periodLabel(mode: PeriodMode, yearMonth: YearMonth, year: Int): String = when (mode) {
        PeriodMode.MONTHLY -> formatMonth(yearMonth)
        PeriodMode.YEARLY -> year.toString()
    }

    fun monthRange(yearMonth: YearMonth): LongRange =
        millisOf(yearMonth.atDay(1)) until millisOf(yearMonth.plusMonths(1).atDay(1))

    fun contains(monthRange: LongRange, millis: Long): Boolean = millis in monthRange
}
