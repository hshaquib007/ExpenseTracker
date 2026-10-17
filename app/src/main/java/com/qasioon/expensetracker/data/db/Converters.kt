package com.qasioon.expensetracker.data.db

import androidx.room.TypeConverter
import com.qasioon.expensetracker.data.model.AccountType
import com.qasioon.expensetracker.data.model.TransactionType

class Converters {

    @TypeConverter
    fun accountTypeToString(value: AccountType): String = value.name

    @TypeConverter
    fun stringToAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun transactionTypeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
