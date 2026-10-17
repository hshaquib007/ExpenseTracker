package com.qasioon.expensetracker.data.repo

import com.qasioon.expensetracker.data.db.AccountDao
import com.qasioon.expensetracker.data.db.CategoryDao
import com.qasioon.expensetracker.data.db.TransactionDao
import com.qasioon.expensetracker.data.entity.AccountEntity
import com.qasioon.expensetracker.data.entity.CategoryEntity
import com.qasioon.expensetracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val dao: AccountDao) {
    fun observeAll(): Flow<List<AccountEntity>> = dao.observeAll()
    fun observeById(id: Long): Flow<AccountEntity?> = dao.observeById(id)
    suspend fun getById(id: Long): AccountEntity? = dao.getById(id)
    suspend fun insert(account: AccountEntity): Long = dao.insert(account)
    suspend fun update(account: AccountEntity) = dao.update(account)
    suspend fun delete(account: AccountEntity) = dao.delete(account)
    suspend fun count(): Int = dao.count()
}

class CategoryRepository(private val dao: CategoryDao) {
    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()
    suspend fun getById(id: Long): CategoryEntity? = dao.getById(id)
    suspend fun insert(category: CategoryEntity): Long = dao.insert(category)
    suspend fun update(category: CategoryEntity) = dao.update(category)
    suspend fun delete(category: CategoryEntity) = dao.delete(category)
    suspend fun count(): Int = dao.count()
}

class TransactionRepository(private val dao: TransactionDao) {
    fun observeInRange(from: Long, to: Long): Flow<List<TransactionEntity>> = dao.observeInRange(from, to)
    fun observeForAccount(accountId: Long): Flow<List<TransactionEntity>> = dao.observeForAccount(accountId)
    fun observeAll(): Flow<List<TransactionEntity>> = dao.observeAll()
    suspend fun getById(id: Long): TransactionEntity? = dao.getById(id)
    suspend fun insert(transaction: TransactionEntity): Long = dao.insert(transaction)
    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)
    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)
    suspend fun count(): Int = dao.count()
}
