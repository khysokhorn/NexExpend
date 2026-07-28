package com.nextgen.expend.data

import com.nextgen.expend.data.local.TransactionDao
import com.nextgen.expend.data.local.toDomain
import com.nextgen.expend.data.local.toEntity
import com.nextgen.expend.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val transactionDao: TransactionDao
) {

    fun getTransactionsFlow(): Flow<List<Transaction>> {
        return transactionDao.getAllFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction.toEntity())
    }
}
