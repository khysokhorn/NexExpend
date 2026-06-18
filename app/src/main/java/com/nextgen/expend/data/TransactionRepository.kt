package com.nextgen.expend.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nextgen.expend.data.model.Transaction
import kh.com.nexgen.base.database.cache.DatabaseCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val databaseCache: DatabaseCache,
    private val gson: Gson
) {
    private val transactionsKey = "transactions_key"

    fun getTransactionsFlow(): Flow<List<Transaction>> {
        return databaseCache.getStringAsFlow(transactionsKey).map { json ->
            if (json == null) {
                // Initialize database with sample data if it's empty
                databaseCache.putObject(transactionsKey, sampleTransactions)
                sampleTransactions
            } else {
                val type = object : TypeToken<List<Transaction>>() {}.type
                try {
                    gson.fromJson<List<Transaction>>(json, type) ?: sampleTransactions
                } catch (e: Exception) {
                    sampleTransactions
                }
            }
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        val current = getTransactionsFlow().first()
        val updated = listOf(transaction) + current
        databaseCache.putObject(transactionsKey, updated)
    }
}
