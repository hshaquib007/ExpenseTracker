package com.qasioon.expensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qasioon.expensetracker.data.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val creditLimit: Double? = null,
    val billDueDay: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
